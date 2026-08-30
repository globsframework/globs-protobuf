# Globs Protobuf

Read and write [Glob](https://globsframework.org)s **directly in the protobuf wire format** — no
`protobuf-java` messages, no `CodedOutputStream`, no descriptors, no generated classes at runtime. The mapping
is carried by an annotation on the `GlobType`, so a Glob is on the wire exactly what a `.proto` message would
be, and the two interoperate byte for byte (the tests write a Glob and parse it with `protobuf-java`, and the
other way round).

The directory is `globs-grpc`; the artifact is **`globs-protobuf`**.

## Requirements

- Java 21
- `org.globsframework:globs`. `protobuf-java` and `io.grpc:*` are **test-scoped only**

## Installation

```xml
<dependency>
    <groupId>org.globsframework</groupId>
    <artifactId>globs-protobuf</artifactId>
    <version>5.5.0</version>
</dependency>
```

## Mapping a GlobType

`ProtobufField` is the annotation — itself a `GlobType`, as everywhere in the ecosystem. Attach it at
declaration time:

```java
GlobTypeBuilder builder = GlobTypeBuilderFactory.create("EchoRequest");
i32     = builder.declareIntegerField("i32", ProtobufField.create(2, ProtobufField.GrpcType.int32));
si32    = builder.declareIntegerField("si32", ProtobufField.create(6, ProtobufField.GrpcType.sint32));
message = builder.declareStringField("message", ProtobufField.create(10));
gstr    = builder.declareStringField("gstrValue", ProtobufField.createValue(33));  // google.protobuf.StringValue
ts      = builder.declareLongField("timestamp", ProtobufField.create(41, ProtobufField.GrpcType.timestamp));
child   = builder.declareGlobArrayField("child", () -> TYPE, ProtobufField.create(12));  // recursive is fine
```

Three attributes: **`number`** (the proto field number), **`type`** (a `GrpcType`: `int32`, `int64`,
`uint32`, `uint64`, `sint32`, `sint64`, `bool`, `enum_`, `fixed32/64`, `sfixed32/64`, `float_`, `double_`,
`timestamp`; `NA` means "the natural encoding for this Java type"), and **`isValue`**
(`ProtobufField.createValue(n)` — the field is a wrapped `google.protobuf.XxxValue` message rather than a
scalar).

A field with no `ProtobufField` annotation is skipped, with a warning when the registry is built.

An unset field is not written — as in protobuf, there is no way to say "explicitly null" on the wire, so
`isSet` survives a round-trip but a written null does not.

## Writing and reading

```java
// write
ProtobufWriter writer = ProtobufWriter.Builder.init().add(EchoRequestType.TYPE).build();
BinaryWriter binaryWriter = BinaryWriter.newHeapInstance(BufferAllocator.create());
writer.write(glob, binaryWriter);
AllocatedBuffer out = binaryWriter.complete();   // array() / position() / limit()

// read
ProtobufReader reader = ProtobufReader.Builder.init(GlobType::instantiate).add(EchoRequestType.TYPE).build();
Glob glob = reader.read(EchoRequestType.TYPE, new SafeHeapReader(ByteBuffer.wrap(bytes)));
```

`ProtobufWriter.create()` / `ProtobufReader.create(instantiator)` are the lazy variants, resolving each type
on first use through a `ConcurrentHashMap`; the `Builder` resolves them up front. `getWriter(type)` /
`getReader(type)` hand back the per-type codec when the type is known at the call site.

Both directions walk the `GlobType` once and build an array of per-field closures — the writer indexed by
field declaration order, the reader by proto field number, dispatched from the tag. Nothing looks a `Field`
up per record. Self-recursive types work: the registries publish the composite before filling its array.

## Performance

The codec asks core for a **caller** (`org.globsframework.core.model.caller`) over its per-field leaves, and
falls back to the loop when nothing can generate one. Add
[globs-generate](https://github.com/globsframework/globs-generate) to the classpath and turn it on:

```
-Dglobs.caller.fromGlob=org.globsframework.model.generator.AsmCallerGeneratorService
-Dglobs.caller.toGlob=org.globsframework.model.generator.AsmCallerWriteGeneratorService
```

Measured on `GeneratedGlobPerfTest` (four different `GlobType`s, JMH):

| | caller off | caller on |
| --- | --- | --- |
| `write`, core `DefaultGlob` | 186.7k ops/s | **254.1k** (+36 %) |
| `write`, generated Glob (object) | 104k | 229k |
| `read`, object flavour | 123.9k | **187.0k** (+51 %) |

Read the 104k against the 186.7k on the row above it: generating the Glob classes alone made this writer
*half as fast* as core's `DefaultGlob`, one accessor class per field being more receivers at the same
megamorphic call site. The caller buys that back and then some — and it does not need the generated Glob
classes to do it. `-Dglobs.caller.*` on plain `DefaultGlob`s is the configuration to prefer.

The per-field leaves are `record`s on purpose: C2 folds a final instance field only for a class it trusts,
and the caller holds each leaf in a `static final`, so the field number becomes a compile-time constant and
the whole tag computation folds. Keep them records, and do not put the shared encoding on a `default` method
of the leaf interface — measured at 229k → 191k, it adds back the interface dispatch the design removes.

`CLAUDE.md` records the strategies that were tried and dropped (a visitor-based writer, `ToGlobCallerAll` on
the write side) so they are not tried again.

## Building

```bash
mvn test                     # generates the test .proto sources; needs network the first time
mvn -o test-compile
mvn test -Dtest=ProtobufWriterImplTest#allFieldType
```

`GrpcType`'s numeric ids are part of the wire contract — the registries switch on the raw `int`. Never
renumber them; add new ones at the end.

## License

Apache License 2.0 — see <https://www.apache.org/licenses/LICENSE-2.0.txt>.

## Links

- [Globs Framework](https://globsframework.org)
- [GitHub repository](https://github.com/globsframework/globs-protobuf)
