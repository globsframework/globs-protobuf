# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this module is

Directory `globs-grpc`, Maven artifact **`org.globsframework:globs-protobuf`**. It maps a `Glob` (globsframework
metamodel object) to/from the **protobuf wire format directly** — it does not use `protobuf-java` messages,
`CodedOutputStream`, descriptors or generated classes at runtime. `protobuf-java` and `io.grpc:*` are `test`-scoped
only: the tests generate `EchoRequest` from `src/test/proto/echo.proto` and assert byte-for-byte interoperability in
both directions (write a Glob → parse with protobuf-java; write with protobuf-java → read into a Glob).

The parent workspace `../CLAUDE.md` describes how the repos relate; read it for the ecosystem conventions.

## Build & test

Java 21, Maven 3.9. `globs` core is at **`5.11-SNAPSHOT`** — the writer needs `model/caller/`, so this module tracks a core
snapshot; there is no reactor with `globsframework/`, so propagating a core change means `mvn install` there first.

```bash
mvn test                                   # full build; needs network the first time (surefire provider + protoc)
mvn -o test-compile                        # offline works once ~/.m2 is warm
mvn test -Dtest=ProtobufWriterImplTest#allFieldType
```

`mvn -o test` works now that the surefire junit-platform provider is in `~/.m2`; it did not when this file was
first written.

`protobuf-maven-plugin` downloads `protoc` and `protoc-gen-grpc-java` binaries for the detected OS (`os-maven-plugin`)
and generates the test sources from `src/test/proto` — the plugin only has `test-compile` executions, `src/main` has no
`.proto`.

`PerfTest` is a JMH benchmark (annotation processor is wired in the compiler plugin); `ProtobufWriterImplTest`'s
`testProtobuf`/`testGlob` are plain loop timings printed to stdout, not assertions.

`GeneratedGlobPerfTest` is the second JMH benchmark: same serialization, but over Globs built by core's
`DefaultGlob` and by the two ASM flavours of `globs-generate` (test-scoped, `5.3-SNAPSHOT`, needs an `mvn install`
in `../globs-generate`). The flavour is a `@Param`, so JMH forks one JVM per flavour and no flavour pollutes the
others' inline caches; `PerfTypeFamily` builds four *different* GlobTypes per flavour, because with a single type
the accessor call sites are monomorphic and the numbers say nothing. `GeneratedGlobSerializationTest` is the plain
JUnit guard around it (the generated flavours really are generated, and all three write identical bytes).

The second `@Param` is `caller`, a `CallerMode` — `OFF` / `ON` being the two `globs.caller.*` services installed
or not. It is a param and not two runs of the suite because both services cache the property they were loaded
with, so the arms have to be forked apart, which is exactly what JMH does per param combination; `CallerMode`
installs the properties and resets the services around `PerfTypeFamily.create`, where the registries resolve
their callers, the same way `GlobFlavour` does it for `globs.builder`. **The axis is not symmetrical**: the
from-Glob side takes the type's own factory first, so OBJECT and PRIMITIVE are already callered on `write` with
`OFF` and only DEFAULT changes there, while the to-Glob side has no such first source and `ON` is what installs
a caller on `read` for *every* flavour — which is not to say it is worth the same to each, see below. `theCallerModeInstallsWhatTheBenchmarkCompares` asserts that per flavour, per mode and per half — an
inert service would have the two arms measure the same thing twice — and
`theCallerModeChangesNothingObservable` that the two arms write the same bytes and read back the same values.
`instantiateAndFill` and `readAllFields` touch no caller and run twice for nothing; pin `-p caller=OFF` for those.

```bash
java -cp target/classes:target/test-classes:$(cat cp.txt) org.openjdk.jmh.Main 'GeneratedGlobPerfTest' -f 1
java -cp ... org.openjdk.jmh.Main 'GeneratedGlobPerfTest.(write|read)$' -p flavour=DEFAULT -f 1   # the caller axis alone
```

## Architecture

### The mapping is carried by a Glob annotation, not by a schema

`ProtobufField` (in the root package) is the annotation type, following the workspace convention that annotations are
themselves `GlobType`s. A field is mapped by attaching it at declaration time:

```java
i32   = builder.declareIntegerField("i32", ProtobufField.create(2, ProtobufField.GrpcType.int32));
gstr  = builder.declareStringField("gstrValue", ProtobufField.createValue(33));   // google.protobuf.StringValue
ts    = builder.declareLongField("timestamp", ProtobufField.create(41, ProtobufField.GrpcType.timestamp));
```

Three attributes: `number` (the proto field number), `type` (a `GrpcType` ordinal — `NA(0)` means "use the natural
encoding for this Java type"), `isValue` (the field is a wrapped `google.protobuf.XxxValue` message, not a scalar).
**The registries switch on the raw `int` type id, not on the enum** (`case 2, 4 ->` …), so `GrpcType`'s numeric ids are
part of the wire contract — never renumber them; add new ones at the end.

Fields with no `ProtobufField` annotation are skipped with a warning at registry-build time.

### Two symmetric halves: `writer/` and `reader/`

Both follow the workspace "no reflection on the hot path" pattern: walk the `GlobType` once, build an array of
per-field closures, then run a plain loop per record.

| | writer | reader |
| --- | --- | --- |
| per-field interface | `ProtoBufGlobSerializer.write(Glob, BinaryWriter)` | `ProtoBufGlobDeserializer.read(MutableGlob, SafeHeapReader)` |
| per-type composite | `ProtoBufGlobSerializerImpl` | `ProtoBufGlobDeserializerImpl` |
| builds the array | `GlobSerializerRegistry` | `GlobDeserializerRegistry` |
| array indexing | **field declaration index**, iterated in order | **proto field number** (sparse, sized `max(number)+1`), dispatched from the tag |
| ~40 leaf impls | `writer/field/ProtoBuf*SerializerImpl` | `reader/field/ProtoBufGlob*DeserializerImpl` |
| public entry point | `ProtobufWriter` | `ProtobufReader` |

Leaf impls never touch `Field` at runtime: the constructor resolves a typed accessor
(`field.getGlobType().getGetAccessor(field)` / `getSetAccessor(field)`) and stores the proto field number. A null value
means "not set" and the field is simply not written — this preserves the `isSet`/`isNull` distinction on round-trip.

**The writer leaves are `record`s, and that is a performance decision, not a style one.** C2 constant-folds a final
*instance* field only for a class it trusts — records, hidden classes, `java.lang.invoke` — since
`TrustFinalNonStaticFields` is off by default; an ordinary class with a `private final` field is not folded, even
when the receiver is a constant. And on the caller path the receiver *is* a constant: the generated caller holds
each leaf in a `static final`, so once `call` is inlined its `this` is a JIT constant and `fieldNumber` becomes a
compile-time constant — which folds the whole tag computation, `writeTag(computeTag(fieldNumber, wireType))` being
inlined right there (checked with `-XX:+PrintInlining`). Measured on `GeneratedGlobPerfTest.write` OBJECT, five
forks each, A/B/A: **224k → 233-235k ops/s, +4 %**, for a mechanical change. The same applies with more to gain to
`ProtoBufGlobFieldGlobSerializer` / `ProtoBufGlobArrayFieldGlobSerializer`, whose `globSerializer` field is a
*call* and not just a value — though there it only pays if `writeMessage` inlines. Two consequences: the
convenience constructor `(Field, number)` now delegates to the canonical one, and a leaf must not gain a
non-component field. `SkipFieldSerializer` stays a plain class: a stateless singleton has nothing to fold.
Turning the two Glob-valued leaves' `grpcNumber` from `Integer` into `int` — measured separately, on purpose —
changes **nothing**: 234.7k ± 3.8k against the 233-235k of the `Integer` version. The unbox was already free,
the boxed field number being a constant `Integer` that C2 folds through anyway. It was kept because every other
leaf takes an `int` and because it removes a null hazard, not for speed; don't expect that one back.

Both registries are `synchronized` and build in two phases (put the composite in the map, *then* fill its array) so
self-recursive types (`EchoRequest children = 12`) terminate.

The leaves implement `ProtoBufFieldSerializer`, not just `ProtoBufGlobSerializer`: on top of `write(Glob, BinaryWriter)`
they carry core's `FromGlobFunction`, i.e. `call(isSet, isNull, value, writer, null)` — the same encoding, handed
the value instead of fetching it through the accessor. `isSet` is ignored: protobuf cannot say "explicitly null", so
a null value is simply not written (unlike globs-bin-serialisation, whose format has a NULL tag). Since
`FromGlobFunction` declares no checked exception, each `call` wraps `IOException` in `UncheckedIOException` and
`ProtoBufGlobSerializerImpl.write` unwraps it.

**Both methods are written out in each leaf, and `ProtoBufFieldSerializer` is deliberately empty.** Factoring the
encoding into a third method the two would call removes no copy — `write` needs the accessor and a null test on the
value it just read, `call` needs neither — and putting the shared step on the *interface* (a `default call`
delegating to a `writeValue`) costs a second interface dispatch on the very path that exists to remove dispatches:
measured, **229k → 191k ops/s** for the object flavour and **209k → 176k** for the primitive one. A `private`
helper inside a leaf would be free (statically bound), which is what the four Glob-valued writers of
globs-bin-serialisation use; on an interface it is not.

`ProtoBufGlobSerializerImpl.initCaller(type)` — called at the end of `GlobSerializerRegistry.create`, not from the
constructor, because the registry publishes the composite before resolving the fields — asks **core**
(`FromGlobCallerFactory.generatedCallerFor("grpc.write", type, …)`) for a `FromGlobCaller` over those leaves, rather than testing
`CallerGlobFactory` itself. That is what makes both ways of getting one reach this module: the type's own factory
under `-Dglobs.builder`, and the `FromGlobCallerService` of `-Dglobs.caller.fromGlob` for the Globs core builds
(`theCallerServiceReachesTheWriterForANonGeneratedType`). `generatedCallerFor` and not `callerFor` : null means
"nobody can generate this", and the loop is a better answer than the `LoopFromGlobCaller` `callerFor` would hand
back, being 10-20 % ahead of it. With `globs-generate` installed
that caller is a generated class holding each leaf in a `static final` field, so the per-field call site is
monomorphic instead of seeing every leaf class in the process. Nothing is asked of a type whose factory generates
nothing, and the caller is guarded by `glob.getClass() == type.instantiate().getClass()`, so a Glob of the right
type from another source — a custom `GlobInstantiator` — takes the loop rather than a `ClassCastException` inside
the generated `call`. One reference compare per glob. Nothing observable distinguishes the two paths,
which is why `ProtoBufGlobSerializerImpl.isCallerBased()` exists and
`GeneratedGlobSerializationTest.theGeneratedFlavoursWriteThroughACaller` asserts it per flavour and per shape.

What it is worth, on `GeneratedGlobPerfTest.write` with four types, caller off → on: **104k → 229k ops/s** for the
object flavour and **141k → 209k** for the primitive one, DEFAULT unchanged around 185-197k (it has no caller — that
it does not move is what says the measurement is measuring the caller). Read the 104k against DEFAULT rather than
against the improvement: generation alone made this writer **half as fast as core's DefaultGlob**, because one
accessor class per field is more receivers at the same megamorphic site. So the caller buys back that penalty and
then some.

That DEFAULT row is *the type factory's* caller, which DefaultGlob has none of; the service of
`-Dglobs.caller.fromGlob` gives it one, and it is worth as much there — `GeneratedGlobPerfTest.write` DEFAULT,
`caller` OFF → ON, one fork: **186.7k → 254.1k ops/s, +36 %**, which puts plain DefaultGlob *ahead* of the
generated flavours (236k / 238k, both unmoved by the param, having their caller already). Not the paradox it
looks like: the accessor of a generated Glob is one class per field, and four types' worth of them at one call
site is what the caller cannot fold away.

**A second, byte-identical writing strategy was tried and dropped before landing** — worth knowing so it is not
tried again. `ProtoBufGlobVisitorSerializerImpl`, reached through a `ProtobufWriter.Builder.initVisitor()`: the
per-type serializer *was* a `FieldValueVisitorWithContext<BinaryWriter>` handed to `glob.accept`, with each field
resolved to an opcode in two `int[]` rather than to a serializer object, so the walk ran inside the Glob's own
`accept` — which for a generated Glob is code belonging to a single type. It attacked exactly what the caller
attacks, from the Glob's side instead of the serializer's, and it was worth +92 % on the object flavour and +35 % on
the primitive one against the accessor writer *before* the caller, −16 % on DefaultGlob (whose `accept` pays a
dispatch on the Field kind while its accessor path was monomorphic). Against the callered accessor writer it wins
nowhere — 222k / 209k / 168k against 229k / 209k / 184k — so it was deleted rather than kept as a second path to
maintain, along with the `SerializerRegistry` interface that only existed to hold the two. The reader has no
equivalent question: it dispatches on the wire tag, not on the Glob.

**The writer was tried on `ToGlobCallerAll` and it loses — do not retry it.** The unrolled write-side
caller is the arm that wins every comparison in globs-generate's `ToGlobCallerPerf`, but those comparisons are
against the *write* side's own loop; here the incumbent is the **read** side's caller, which is a better
instrument for serializing: it reads the values straight out of the generated Glob's fields and hands them to
the leaf, where `ToGlobCallerAll` has each leaf fetch through its accessor. Prototyped (each leaf's
`call(MutableGlob, BinaryWriter, …)` delegating to its `write`, the composite building the caller from the
array): **235.7k → 213.3k ops/s on `write` OBJECT, −9.5 %**, five forks each, same build, bytes identical.

There is a type-level objection on top of the measurement: `ToGlobFunction.call` takes a `MutableGlob`
because the to-Glob side of the SPI is meant for a *parser*, and a serializer only has a `Glob` — adopting it
means a `checkcast` on the hot path that any read-only Glob implementation would fail.

### The reader has a caller too, and it is the *write* half of the SPI

A parser filling a `MutableGlob` is what `model/caller` describes, so the read loop maps onto
`ToGlobCaller` one piece at a time — and it barely needed adapting:

| the SPI wants | here |
| --- | --- |
| `KeySource.nextKey()` | `SafeHeapReader.getFieldNumber()`, which already decodes the tag and already answers `Integer.MAX_VALUE` at the end of a message. `nextKey` is that, with the checked exception wrapped |
| the key of each `ToGlobFunction` | the proto field number, i.e. the index of `ProtoBufGlobDeserializerImpl`'s array |
| the fallback | `SkipFieldDeserializer`, which skips — what the array path does for a null entry |
| `endLoop` | `Integer.MAX_VALUE` |

`ProtoBufFieldDeserializer` is the leaf interface that carries it (`ProtoBufGlobDeserializer` +
`ToGlobFunction<SafeHeapReader, Void, Void>`), exactly as `ProtoBufFieldSerializer` carries
`FromGlobFunction` on the writer side, and each leaf writes its own one-line `call` delegating to its `read`
— statically bound on a final class, where a `default` on the interface would be the second interface dispatch
this exists to remove. `read` declares `IOException` and `ToGlobFunction` declares nothing, so `call`
wraps and `ProtoBufGlobDeserializerImpl.read` unwraps. `initCaller(type)` runs at the end of
`GlobDeserializerRegistry.create`, after the array is filled — the registry publishes the composite before
resolving the fields, for recursive types.

Both `create` calls take a **name** since `globs` 5.12 (`CallerName` in core) : it is what a generating
implementation names the class it emits after, and therefore what makes that class the same one from one run
to the next — an AOT cache matches a class on its name and its bytes, and the counter this replaced matched
nothing. On the to-Glob side it has to carry the type (`"grpc.read." + type.getName()`), which is the only
reason `initCaller` takes a `GlobType` at all : a write caller is built from functions alone, so nothing else
tells one type's deserializers from another's. On the from-Glob side `"grpc.write"` is enough, `generatedCallerFor`
adding the type. Build it from something constant in the source — a name that varies per run is accepted and
silently gives up the identity it asked for.

**Measured, `GeneratedGlobPerfTest.read` OBJECT, five forks per arm, same build: 123.9k → 187.0k ops/s,
+51 %.** That is the caller alone, the leaves being records already; and it is why they are records — the
previous commit measured that conversion at exactly nothing (132.6k → 130.1k) while the dispatch went through
`attributes[tag]`, with no constant receiver to fold. Same trade as everywhere: the fallback path pays a
little, the array arm dropping ~5 % (130.1k → 123.9k) for the extra super-interface on the leaves and one
`caller != null` per glob.

It needs **`-Dglobs.caller.toGlob=org.globsframework.model.generator.AsmCallerWriteGeneratorService`**, is
independent of `globs.builder` (nothing in the emitted switch reads a Glob's layout — the leaves write through
`MutableGlob`, so there is no guard on the Glob's class here, unlike the writer's caller), and asks
`generated()` rather than `get()`: an array indexed by field number beats the looped
`LoopToGlobCallerFactory` and its binary search, so the loop is not the fallback this wants. That property is
the `ON` arm of the benchmark's `caller` param, which installs a caller here for every flavour — but only the
generated ones gain from it. One fork, four types: OBJECT **124.1k → 172.7k**, DEFAULT **212.4k → 208.3k**,
i.e. nothing outside the noise for DefaultGlob, whose `Object[]` setters stay megamorphic behind whatever
dispatches to them; the constant receiver only pays where the leaf's accessor is a field access it can fold.
Read that beside the writer, where it is DEFAULT that gains and the generated flavours that do not: the caller
buys the second half of a monomorphic path, never the first. The round-trip tests are what say the two paths
read the same bytes.

Two ways to obtain a reader/writer, differing only in the backing map:

- `ProtobufWriter.Builder.init().add(TYPE).build()` / `ProtobufReader.Builder.init(instantiator).add(TYPE).build()` —
  pre-resolve every type at startup, then serve from an immutable `HashMap` with no locking. Prefer this.
- `ProtobufWriter.create()` / `ProtobufReader.create(instantiator)` — lazy, `ConcurrentHashMap`, resolves unknown types
  on first use.

Reader construction needs a `GlobInstantiator` (`GlobType::instantiate` in tests) — it is what allocates the nested
`MutableGlob`s, so alternative Glob implementations (e.g. `globs-generate`) plug in here.

### `BinaryWriter` writes backwards

`BinaryWriter` (and `SafeHeapReader`) are adapted from Google's own `BinaryWriter`/`SafeHeapReader`, kept under their
BSD header. The writer serializes **in reverse** (`fieldOrder() == DESCENDING`, tag written *after* the payload) so a
nested message's length is known without a pre-pass over it — hence `writeMessage` records `getTotalBytesWritten()`,
writes the body, then prepends the varint length and tag. `writeHeaderValue(fieldNumber, indexEnd)` is the same trick
exposed for the hand-rolled wrapper/timestamp serializers.

Consequences to keep in mind:

- Buffers come from a `BufferAllocator` in `DEFAULT_CHUNK_SIZE` (4096) chunks. `complete()` returns the **last-allocated**
  buffer, which holds the *start* of the message; the rest of the message is reached through `AllocatedBuffer.getNext()`.
  Each buffer's payload is `[position(), limit())`. A test that only reads `complete().array()` is implicitly assuming
  the message fit in one chunk.
- Fields end up on the wire in reverse declaration order (legal protobuf, but surprising when eyeballing bytes).
- The writer is single-use per `complete()`, and not thread-safe; the `ProtobufWriter` itself is (once built).

Repeated numeric fields are always written **packed**; the reader accepts both packed and unpacked (it switches on the
tag's wire type) and grows reusable scratch arrays (`intBuffers`/`longBuffers`/`doubleBuffers`) on the `SafeHeapReader`.

### Type mapping specifics

- `float`/`sfixed`/`fixed` variants all live on the ordinary Glob types: a proto `float` is a `DoubleField` with
  `GrpcType.float_`, an `enum` is an `IntegerField` with `GrpcType.enum_`.
- `isValue` (`ProtobufField.createValue`) means the wrapper messages `google.protobuf.{String,Int32,Int64,UInt32,UInt64,Bool,Double}Value`:
  written as a length-delimited submessage containing field `1`, read via `readValueHeader()`/`endValueHeader()`.
  It also gives proto3 `optional` semantics — an unset Glob field emits nothing.
- `GrpcType.timestamp` maps a `LongField` holding **epoch millis** to `google.protobuf.Timestamp`
  (`seconds = v / 1000`, `nanos = v % 1000 * 1_000_000`).
- Unsupported `Field` kinds hit `default -> throw new IllegalStateException` in both registries: `BytesField`,
  `DateField`, `DateTimeField`, `BigDecimal*Field`, `GlobUnionField`, `GlobArrayUnionField`. Adding one means a leaf
  serializer + a leaf deserializer + a `case` in each registry, and possibly a wire primitive in `BinaryWriter`/`SafeHeapReader`.

### Known rough edges

- Duplicate proto field numbers are detected on the reader side only (`throwDuplicate`); the writer would silently emit
  both.
