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

Java 21, Maven 3.9. `globs` core is pinned to a released version (`5.5.0`), resolved from `~/.m2` or GitHub Packages —
there is no reactor with `globsframework/`; propagating a core change means `mvn install` there first.

```bash
mvn test                                   # full build; needs network the first time (surefire provider + protoc)
mvn -o test-compile                        # offline works once ~/.m2 is warm
mvn test -Dtest=ProtobufWriterImplTest#allFieldType
```

`mvn -o test` currently fails: the surefire junit-platform provider is not in `~/.m2`. Use online `mvn test`.

`protobuf-maven-plugin` downloads `protoc` and `protoc-gen-grpc-java` binaries for the detected OS (`os-maven-plugin`)
and generates the test sources from `src/test/proto` — the plugin only has `test-compile` executions, `src/main` has no
`.proto`.

`PerfTest` is a JMH benchmark (annotation processor is wired in the compiler plugin); `ProtobufWriterImplTest`'s
`testProtobuf`/`testGlob` are plain loop timings printed to stdout, not assertions.

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

Both registries are `synchronized` and build in two phases (put the composite in the map, *then* fill its array) so
self-recursive types (`EchoRequest children = 12`) terminate.

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
