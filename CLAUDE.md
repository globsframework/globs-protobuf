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

Java 21, Maven 3.9. `globs` core is at **`5.11-SNAPSHOT`** — the writer needs `model/generate/read/`, so this module tracks a core
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

```bash
java -cp target/classes:target/test-classes:$(cat cp.txt) org.openjdk.jmh.Main 'GeneratedGlobPerfTest' -f 1
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
they carry core's `FieldValueFunction`, i.e. `call(isSet, isNull, value, writer, null)` — the same encoding, handed
the value instead of fetching it through the accessor. `isSet` is ignored: protobuf cannot say "explicitly null", so
a null value is simply not written (unlike globs-bin-serialisation, whose format has a NULL tag). Since
`FieldValueFunction` declares no checked exception, each `call` wraps `IOException` in `UncheckedIOException` and
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
(`GenerateCaller.generatedCallerFor`) for a `GeneratedFunctionCaller` over those leaves, rather than testing
`GlobGenerateFactory` itself. That is what makes both ways of getting one reach this module: the type's own factory
under `-Dglobs.builder`, and the `GenerateCallerService` of `-Dglobs.caller` for the Globs core builds
(`theCallerServiceReachesTheWriterForANonGeneratedType`). `generatedCallerFor` and not `callerFor` : null means
"nobody can generate this", and the loop is a better answer than the `DefaultFunctionCaller` `callerFor` would hand
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

**The `ProtoBufGlobDeserializer` leaves are records too, and that one buys nothing yet — deliberately.** The
folding that makes the writer leaves worth +4 % needs a *constant receiver*, which they get from the generated
caller's `static final`s. `ProtoBufGlobDeserializerImpl.read` dispatches through `attributes[tag]`, an array
element: nothing there is a JIT constant, so nothing folds. Measured, five forks each: `read` 132.6k → 130.1k
ops/s, i.e. no change (the untouched `readAllFields` moves as much, 423.8k → 426.4k). It is kept for symmetry
with the writers and because it is what makes the *next* step pay, not for what it does today.

That next step is the one globs-bin-serialisation took: its reader now drives core's `GeneratedCallerWrite` —
the read loop is exactly that shape, a `CallAtWrite` answering the next field number and one
`MutableFunctionWrite` per number — for **+17 %**, and the record leaves then added **+3.8 % to +12.3 %** on top
of it. Here it would mean `ProtoBufGlobDeserializer` extending `MutableFunctionWrite`, `SafeHeapReader` playing
the `CallAtWrite` (its `getFieldNumber()` already *is* `getNextToCall`, `Integer.MAX_VALUE` already being the
end sentinel), and `-Dglobs.callerWrite` to install the generator.

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
