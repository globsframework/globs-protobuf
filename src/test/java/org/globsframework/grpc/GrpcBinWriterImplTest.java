package org.globsframework.grpc;


import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.*;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.utils.NanoChrono;
import org.globsframework.grpc.echo.EchoRequest;
import org.globsframework.grpc.reader.ProtobufReader;
import org.globsframework.grpc.reader.ProtobufReaderImpl;
import org.globsframework.grpc.reader.SafeHeapReader;
import org.globsframework.grpc.writer.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Queue;

public class GrpcBinWriterImplTest {

    @Test
    void name() throws IOException {

        final EchoRequest echoRequest = buildGrpRequest();

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        echoRequest.writeTo(output);
        ProtobufReader protobufReader = new ProtobufReaderImpl(GlobType::instantiate);
        final Glob data = protobufReader.read(EchoRequestType.TYPE,
                new SafeHeapReader(ByteBuffer.wrap(output.toByteArray()), true));
        Assertions.assertNotNull(data);
        Assertions.assertEquals(1, data.get(EchoRequestType.i32));
        Assertions.assertEquals(341, data.get(EchoRequestType.fi32));
        Assertions.assertEquals(-341, data.get(EchoRequestType.sfi32));
        Assertions.assertEquals(21451, data.get(EchoRequestType.ui32));
        Assertions.assertEquals(1321, data.get(EchoRequestType.si32));

        Assertions.assertArrayEquals(new int[]{1, 2, 4}, data.get(EchoRequestType.i32values));
        Assertions.assertArrayEquals(new int[]{3, 2, 4}, data.get(EchoRequestType.si32values));
        Assertions.assertArrayEquals(new int[]{5, 2, 4}, data.get(EchoRequestType.ui32values));
        Assertions.assertArrayEquals(new int[]{7, 2, 4}, data.get(EchoRequestType.fi32values));
        Assertions.assertArrayEquals(new int[]{9, 2, 4}, data.get(EchoRequestType.sfi32values));

        Assertions.assertEquals(1, data.get(EchoRequestType.i64));
        Assertions.assertEquals(341, data.get(EchoRequestType.fi64));
        Assertions.assertEquals(-341, data.get(EchoRequestType.sfi64));
        Assertions.assertEquals(21451, data.get(EchoRequestType.ui64));
        Assertions.assertEquals(1321, data.get(EchoRequestType.si64));

        Assertions.assertArrayEquals(new long[]{1, 2, 4}, data.get(EchoRequestType.i64values));
        Assertions.assertArrayEquals(new long[]{3, 2, 4}, data.get(EchoRequestType.si64values));
        Assertions.assertArrayEquals(new long[]{5, 2, 4}, data.get(EchoRequestType.ui64values));
        Assertions.assertArrayEquals(new long[]{7, 2, 4}, data.get(EchoRequestType.fi64values));
        Assertions.assertArrayEquals(new long[]{9, 2, 4}, data.get(EchoRequestType.sfi64values));
        Assertions.assertTrue(data.get(EchoRequestType.bValue));
        Assertions.assertArrayEquals(new boolean[]{true, false}, data.get(EchoRequestType.bBalues));

        Assertions.assertNotNull(data.get(EchoRequestType.children));
        final Glob children = data.get(EchoRequestType.children);
        Assertions.assertEquals(10, children.get(EchoRequestType.i32));
        Assertions.assertEquals(324, children.get(EchoRequestType.si32));

        final Glob[] chs = data.get(EchoRequestType.child);
        Assertions.assertEquals(2, chs.length);
        Assertions.assertEquals(10, chs[0].get(EchoRequestType.i32));
        Assertions.assertEquals(324, chs[0].get(EchoRequestType.si32));
        Assertions.assertEquals(20, chs[1].get(EchoRequestType.i32));
        Assertions.assertEquals(5678, chs[1].get(EchoRequestType.si32));

        Assertions.assertEquals("mon message", data.get(EchoRequestType.message));
        Assertions.assertArrayEquals(new String[]{"message 1", "message 2"}, data.get(EchoRequestType.messages));

        Assertions.assertEquals(1.22, data.get(EchoRequestType.dValue));
        Assertions.assertArrayEquals(new double[]{4.2, -3.2}, data.get(EchoRequestType.dValues), 0.000001);


        Assertions.assertEquals(1.534f, data.get(EchoRequestType.fValue), 0.000001);
        Assertions.assertArrayEquals(new double[]{4.4, 5.4}, data.get(EchoRequestType.fValues), 0.000001);

        GrpcBinWriter protobufWriter = new ProtobufWriterImpl(new GlobSerializerRegistry());
        final BufferAllocator alloc = new BufferAllocator();
        final BinaryWriter writer = BinaryWriter.newHeapInstance(alloc, protobufWriter);
        protobufWriter.write(data, writer);
        final Queue<AllocatedBuffer> complete = writer.complete();
        EchoRequest.Builder builderRead = EchoRequest.newBuilder();
        final AllocatedBuffer element = complete.element();
        builderRead.mergeFrom(element.array(), element.position(), element.limit() - element.position());
        final EchoRequest readData = builderRead.build();
        Assertions.assertEquals(1, readData.getI32());
        Assertions.assertEquals(341, readData.getFi32());
        Assertions.assertEquals(-341, readData.getSf32());
        Assertions.assertEquals(21451, readData.getUi32());
        Assertions.assertEquals(1321, readData.getSi32());

        Assertions.assertEquals(List.of(1, 2, 4), readData.getI32ValuesList());
        Assertions.assertEquals(List.of(3, 2, 4), readData.getSi32ValuesList());
        Assertions.assertEquals(List.of(5, 2, 4), readData.getUi32ValuesList());
        Assertions.assertEquals(List.of(7, 2, 4), readData.getFi32ValuesList());
        Assertions.assertEquals(List.of(9, 2, 4), readData.getSfi32ValuesList());


        Assertions.assertEquals(1, readData.getI64());
        Assertions.assertEquals(341, readData.getFi64());
        Assertions.assertEquals(-341, readData.getSf64());
        Assertions.assertEquals(21451, readData.getUi64());
        Assertions.assertEquals(1321, readData.getSi64());

        Assertions.assertEquals(List.of(1L, 2L, 4L), readData.getI64ValuesList());
        Assertions.assertEquals(List.of(3L, 2L, 4L), readData.getSi64ValuesList());
        Assertions.assertEquals(List.of(5L, 2L, 4L), readData.getUi64ValuesList());
        Assertions.assertEquals(List.of(7L, 2L, 4L), readData.getFi64ValuesList());
        Assertions.assertEquals(List.of(9L, 2L, 4L), readData.getSfi64ValuesList());

        Assertions.assertTrue(readData.getBValue());
        Assertions.assertEquals(List.of(true, false), readData.getBValuesList());

        final EchoRequest ch = readData.getChildren();
        Assertions.assertNotNull(ch);
        Assertions.assertEquals(10, ch.getI32());
        Assertions.assertEquals(324, ch.getSi32());

        final List<EchoRequest> childList = readData.getChildList();
        Assertions.assertEquals(2, childList.size());
        Assertions.assertEquals(10, childList.get(0).getI32());
        Assertions.assertEquals(324, childList.get(0).getSi32());
        Assertions.assertEquals(20, childList.get(1).getI32());
        Assertions.assertEquals(5678, childList.get(1).getSi32());

        Assertions.assertEquals("mon message", readData.getMessage());
        Assertions.assertEquals(List.of("message 1", "message 2"), readData.getMessagesList());

        Assertions.assertEquals(1.22, readData.getD64(), 0.000001);
        Assertions.assertEquals(List.of(4.2, -3.2), readData.getD64ValuesList());

        Assertions.assertEquals(1.534f, readData.getF32(), 0.000001);
        Assertions.assertEquals(List.of(4.4f, 5.4f), readData.getF32ValuesList());
    }

    public static Glob buildGlobRequest() {
        final MutableGlob ch1 = EchoRequestType.TYPE.instantiate();
        ch1.set(EchoRequestType.i32, 10);
        ch1.set(EchoRequestType.si32, 324);

        final MutableGlob ch2 = EchoRequestType.TYPE.instantiate();
        ch2.set(EchoRequestType.i32, 20);
        ch2.set(EchoRequestType.si32, 5678);

        final MutableGlob main = EchoRequestType.TYPE.instantiate();
        main.set(EchoRequestType.i32, 1);
        main.set(EchoRequestType.fi32, 341);
        main.set(EchoRequestType.sfi32, -341);
        main.set(EchoRequestType.ui32, 21451);
        main.set(EchoRequestType.si32, 1321);
        main.set(EchoRequestType.i32values, new int[]{1, 2, 4});
        main.set(EchoRequestType.si32values, new int[]{3, 2, 4});
        main.set(EchoRequestType.ui32values, new int[]{5, 2, 4});
        main.set(EchoRequestType.fi32values, new int[]{7, 2, 4});
        main.set(EchoRequestType.sfi32values, new int[]{9, 2, 4});
        main.set(EchoRequestType.i64, 1);
        main.set(EchoRequestType.fi64, 341);
        main.set(EchoRequestType.sfi64, -341);
        main.set(EchoRequestType.ui64, 21451);
        main.set(EchoRequestType.si64, 1321);
        main.set(EchoRequestType.i64values, new long[]{1L, 2L, 4L});
        main.set(EchoRequestType.si64values, new long[]{3L, 2L, 4L});
        main.set(EchoRequestType.ui64values, new long[]{5L, 2L, 4L});
        main.set(EchoRequestType.fi64values, new long[]{7L, 2L, 4L});
        main.set(EchoRequestType.sfi64values, new long[]{9L, 2L, 4L});

        main.set(EchoRequestType.bValue, true);
        main.set(EchoRequestType.bBalues, new boolean[]{true, false});
        main.set(EchoRequestType.message, "mon message");

        main.set(EchoRequestType.messages, new String[]{"message 1", "message 2"});

        main.set(EchoRequestType.dValue, 1.22);
        main.set(EchoRequestType.dValues, new double[]{4.2, -3.2});
        main.set(EchoRequestType.fValue, 1.534f);
        main.set(EchoRequestType.fValues, new double[]{4.4, 5.4});
        main.set(EchoRequestType.children, ch1);
        main.set(EchoRequestType.child, new Glob[]{ch1, ch2});

        return main;
    }

    public static EchoRequest buildGrpRequest() {
        EchoRequest.Builder ch1 = EchoRequest.newBuilder();
        ch1.setI32(10);
        ch1.setSi32(324);

        EchoRequest.Builder ch2 = EchoRequest.newBuilder();
        ch2.setI32(20);
        ch2.setSi32(5678);

        EchoRequest.Builder builder = EchoRequest.newBuilder();
        builder.setI32(1);
        builder.setFi32(341);
        builder.setSf32(-341);
        builder.setUi32(21451);
        builder.setSi32(1321);
        builder.addAllI32Values(List.of(1, 2, 4));
        builder.addAllSi32Values(List.of(3, 2, 4));
        builder.addAllUi32Values(List.of(5, 2, 4));
        builder.addAllFi32Values(List.of(7, 2, 4));
        builder.addAllSfi32Values(List.of(9, 2, 4));

        builder.setI64(1);
        builder.setFi64(341);
        builder.setSf64(-341);
        builder.setUi64(21451);
        builder.setSi64(1321);
        builder.addAllI64Values(List.of(1L, 2L, 4L));
        builder.addAllSi64Values(List.of(3L, 2L, 4L));
        builder.addAllUi64Values(List.of(5L, 2L, 4L));
        builder.addAllFi64Values(List.of(7L, 2L, 4L));
        builder.addAllSfi64Values(List.of(9L, 2L, 4L));

        builder.setBValue(true);
        builder.addBValues(true);
        builder.addBValues(false);

        builder.setMessage("mon message");
        builder.addMessages("message 1");
        builder.addMessages("message 2");

        builder.setD64(1.22);
        builder.addD64Values(4.2);
        builder.addD64Values(-3.2);

        builder.setF32(1.534f);
        builder.addF32Values(4.4f);
        builder.addF32Values(5.4f);

        builder.setChildren(ch1.build());

        builder.addChild(ch1.build());
        builder.addChild(ch2.build());

        final EchoRequest echoRequest = builder.build();
        return echoRequest;
    }

    static class EchoRequestType {
        public static final GlobType TYPE;

        public static final StringField message;
        public static final StringArrayField messages;

        public static final IntegerField i32;
        public static final IntegerField si32;
        public static final IntegerField ui32;
        public static final IntegerField fi32;
        public static final IntegerField sfi32;
        public static final IntegerArrayField i32values;
        public static final IntegerArrayField si32values;
        public static final IntegerArrayField ui32values;
        public static final IntegerArrayField fi32values;
        public static final IntegerArrayField sfi32values;

        public static final LongField i64;
        public static final LongField si64;
        public static final LongField ui64;
        public static final LongField fi64;
        public static final LongField sfi64;
        public static final LongArrayField i64values;
        public static final LongArrayField si64values;
        public static final LongArrayField ui64values;
        public static final LongArrayField fi64values;
        public static final LongArrayField sfi64values;

        public static final BooleanField bValue;
        public static final BooleanArrayField bBalues;

        public static final DoubleField dValue;
        public static final DoubleArrayField dValues;

        public static final DoubleField fValue;
        public static final DoubleArrayField fValues;

        public static final GlobField children;

        public static final GlobArrayField child;

        static {
            final GlobTypeBuilder builder = GlobTypeBuilderFactory.create("EchoRequestType");
            TYPE = builder.unCompleteType();
            i32 = builder.declareIntegerField("i32", GrpcField.create(2, GrpcField.GrpcType.int32));
            si32 = builder.declareIntegerField("si32", GrpcField.create(3, GrpcField.GrpcType.sint32));
            ui32 = builder.declareIntegerField("ui32", GrpcField.create(4, GrpcField.GrpcType.uint32));
            fi32 = builder.declareIntegerField("fi32", GrpcField.create(5, GrpcField.GrpcType.fixed32));
            sfi32 = builder.declareIntegerField("sf32", GrpcField.create(6, GrpcField.GrpcType.sfixed32));
            i32values = builder.declareIntegerArrayField("i32values", GrpcField.create(7, GrpcField.GrpcType.int32));
            si32values = builder.declareIntegerArrayField("si32values", GrpcField.create(8, GrpcField.GrpcType.sint32));
            ui32values = builder.declareIntegerArrayField("ui32values", GrpcField.create(9, GrpcField.GrpcType.uint32));
            fi32values = builder.declareIntegerArrayField("fi32values", GrpcField.create(10, GrpcField.GrpcType.fixed32));
            sfi32values = builder.declareIntegerArrayField("sf32values", GrpcField.create(11, GrpcField.GrpcType.sfixed32));
            i64 = builder.declareLongField("i64", GrpcField.create(14, GrpcField.GrpcType.int64));
            si64 = builder.declareLongField("si64", GrpcField.create(15, GrpcField.GrpcType.sint64));
            ui64 = builder.declareLongField("ui64", GrpcField.create(16, GrpcField.GrpcType.uint64));
            fi64 = builder.declareLongField("fi64", GrpcField.create(17, GrpcField.GrpcType.fixed64));
            sfi64 = builder.declareLongField("sf64", GrpcField.create(18, GrpcField.GrpcType.sfixed64));
            i64values = builder.declareLongArrayField("i64values", GrpcField.create(19, GrpcField.GrpcType.int64));
            si64values = builder.declareLongArrayField("si64values", GrpcField.create(20, GrpcField.GrpcType.sint64));
            ui64values = builder.declareLongArrayField("ui64values", GrpcField.create(21, GrpcField.GrpcType.uint64));
            fi64values = builder.declareLongArrayField("fi64values", GrpcField.create(22, GrpcField.GrpcType.fixed64));
            sfi64values = builder.declareLongArrayField("sf64values", GrpcField.create(23, GrpcField.GrpcType.sfixed64));
            children = builder.declareGlobField("children", EchoRequestType.TYPE, GrpcField.create(12));
            child = builder.declareGlobArrayField("child", EchoRequestType.TYPE, GrpcField.create(13));
            bValue = builder.declareBooleanField("bValue", GrpcField.create(24));
            bBalues = builder.declareBooleanArrayField("bBalues", GrpcField.create(25));
            message = builder.declareStringField("message", GrpcField.create(26));
            messages = builder.declareStringArrayField("messages", GrpcField.create(27));
            dValue = builder.declareDoubleField("dValue", GrpcField.create(28));
            dValues = builder.declareDoubleArrayField("dValues", GrpcField.create(29));
            fValue = builder.declareDoubleField("fValue", GrpcField.create(30, GrpcField.GrpcType.float_));
            fValues = builder.declareDoubleArrayField("fValues", GrpcField.create(31, GrpcField.GrpcType.float_));
            builder.complete();
        }
    }

    @Test
    void basic() throws IOException {
        Glob glob = GrpcBinWriterImplTest.buildGlobRequest();
        EchoRequest echoRequest = GrpcBinWriterImplTest.buildGrpRequest();
        ProtobufWriterImpl grpcBinWriter = new ProtobufWriterImpl(new GlobSerializerRegistry());
        echoRequest.writeTo(new ByteArrayOutputStream());
        grpcBinWriter.write(glob, BinaryWriter.newHeapInstance(new BufferAllocator(), grpcBinWriter, 1024));


    }

    @Test
    public void testProtobuf() throws IOException {
        NanoChrono nanoChrono = NanoChrono.start();
        for (int i = 0; i < 1_000_000; i++) {
            EchoRequest echoRequest = GrpcBinWriterImplTest.buildGrpRequest();
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            echoRequest.writeTo(output);
            Assertions.assertNotNull(output.toByteArray());
        }
        System.out.println("protobuf : "+ nanoChrono.getElapsedTimeInMS());
    }

    final public ProtobufWriterImpl grpcBinWriter;
    final GlobSerializerRegistry registry;

    {
        registry = new GlobSerializerRegistry();
        grpcBinWriter = new ProtobufWriterImpl(registry);
    }

    @Test
    public void testGlob() throws IOException {
        NanoChrono nanoChrono = NanoChrono.start();
        final ProtoBufGlobSerializer globSerializer = registry.getGlobSerializer(EchoRequestType.TYPE);
        for (int i = 0; i < 1_000_000; i++) {
            Glob glob = GrpcBinWriterImplTest.buildGlobRequest();
            final BufferAllocator alloc = new BufferAllocator();
            final BinaryWriter writer = BinaryWriter.newHeapInstance(alloc, grpcBinWriter, 1024);
            globSerializer.write(glob, writer);
            Assertions.assertNotNull(writer.complete().element().array());
        }
        System.out.println("glob " + nanoChrono.getElapsedTimeInMS());
    }

    static {
        System.setProperty("globsframework.field.no.check", "true");
    }
}
