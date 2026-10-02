package org.globsframework.grpc;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.grpc.ProtobufWriterImplTest.EchoRequestType;
import org.globsframework.grpc.echo.EchoRequest;
import org.globsframework.grpc.reader.ProtobufReader;
import org.globsframework.grpc.reader.SafeHeapReader;
import org.globsframework.grpc.writer.AllocatedBuffer;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.BufferAllocator;
import org.globsframework.grpc.writer.ProtobufWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * The property-based reader/writer (Builder.buildUseProperties()) must produce exactly what the
 * map-based ones (Builder.build()) produce, both through read/write and through getReader/getWriter.
 */
public class PropertyBasedProtobufTest {

    @Test
    void writerProducesSameBytesAsMapBasedWriter() throws IOException {
        final Glob glob = ProtobufWriterImplTest.buildGlobRequest();

        final byte[] expected = write(ProtobufWriter.Builder.init().add(EchoRequestType.TYPE).build(), glob);
        final ProtobufWriter propertyWriter = ProtobufWriter.Builder.init().add(EchoRequestType.TYPE).buildUseProperties();

        Assertions.assertArrayEquals(expected, write(propertyWriter, glob));

        final BinaryWriter writer = BinaryWriter.newHeapInstance(BufferAllocator.create());
        propertyWriter.getWriter(EchoRequestType.TYPE).write(glob, writer);
        Assertions.assertArrayEquals(expected, toBytes(writer.complete()));

        final EchoRequest parsed = EchoRequest.parseFrom(expected);
        Assertions.assertEquals(1321, parsed.getSi32());
        Assertions.assertEquals(2, parsed.getChildCount());
    }

    @Test
    void writerIgnoresNullGlob() throws IOException {
        final ProtobufWriter propertyWriter = ProtobufWriter.Builder.init().buildUseProperties();
        Assertions.assertEquals(0, write(propertyWriter, null).length);
    }

    @Test
    void readerReadsSameGlobAsMapBasedReader() throws IOException {
        final byte[] bytes = ProtobufWriterImplTest.buildGrpRequest().toByteArray();

        final Glob expected = ProtobufReader.Builder.init(GlobType::instantiate).add(EchoRequestType.TYPE).build()
                .read(EchoRequestType.TYPE, new SafeHeapReader(ByteBuffer.wrap(bytes)));
        final ProtobufReader propertyReader = ProtobufReader.Builder.init(GlobType::instantiate)
                .add(EchoRequestType.TYPE).buildUseProperties();

        final Glob read = propertyReader.read(EchoRequestType.TYPE, new SafeHeapReader(ByteBuffer.wrap(bytes)));
        final Glob readByReader = propertyReader.getReader(EchoRequestType.TYPE)
                .read(EchoRequestType.TYPE, new SafeHeapReader(ByteBuffer.wrap(bytes)));

        Assertions.assertEquals(1321, read.get(EchoRequestType.si32));
        Assertions.assertEquals("mon message", read.get(EchoRequestType.message));
        Assertions.assertEquals(2, read.get(EchoRequestType.child).length);
        Assertions.assertFalse(read.get(EchoRequestType.children).isSet(EchoRequestType.io32));

        // compare through a re-encoding: every mapped field takes part
        final ProtobufWriter writer = ProtobufWriter.create();
        final byte[] expectedBytes = write(writer, expected);
        Assertions.assertArrayEquals(expectedBytes, write(writer, read));
        Assertions.assertArrayEquals(expectedBytes, write(writer, readByReader));
    }

    @Test
    void typeNotDeclaredInBuilderIsBuiltOnFirstUse() throws IOException {
        final ProtobufWriter propertyWriter = ProtobufWriter.Builder.init().buildUseProperties();
        final ProtobufReader propertyReader = ProtobufReader.Builder.init(GlobType::instantiate).buildUseProperties();

        final MutableGlob glob = LazyType.TYPE.instantiate()
                .set(LazyType.id, 42)
                .set(LazyType.name, "lazy");

        final byte[] bytes = write(propertyWriter, glob);
        // a second call goes through the value cached on the GlobType
        Assertions.assertArrayEquals(bytes, write(propertyWriter, glob));

        final Glob read = propertyReader.read(LazyType.TYPE, new SafeHeapReader(ByteBuffer.wrap(bytes)));
        Assertions.assertEquals(42, read.get(LazyType.id));
        Assertions.assertEquals("lazy", read.get(LazyType.name));

        final Glob readAgain = propertyReader.getReader(LazyType.TYPE)
                .read(LazyType.TYPE, new SafeHeapReader(ByteBuffer.wrap(bytes)));
        Assertions.assertEquals(42, readAgain.get(LazyType.id));
        Assertions.assertEquals("lazy", readAgain.get(LazyType.name));
    }

    private static byte[] write(ProtobufWriter protobufWriter, Glob glob) throws IOException {
        final BinaryWriter writer = BinaryWriter.newHeapInstance(BufferAllocator.create());
        protobufWriter.write(glob, writer);
        return toBytes(writer.complete());
    }

    private static byte[] toBytes(AllocatedBuffer buffer) {
        return Arrays.copyOfRange(buffer.array(), buffer.position(), buffer.limit());
    }

    // only used by typeNotDeclaredInBuilderIsBuiltOnFirstUse, so its property is unset until that test
    static class LazyType {
        public static final GlobType TYPE;
        public static final IntegerField id;
        public static final StringField name;

        static {
            final GlobTypeBuilder builder = GlobTypeBuilderFactory.create("LazyType");
            id = builder.declareIntegerField("id", ProtobufField.create(1));
            name = builder.declareStringField("name", ProtobufField.create(2));
            TYPE = builder.build();
        }
    }
}
