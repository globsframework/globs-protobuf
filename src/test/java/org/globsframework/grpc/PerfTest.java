package org.globsframework.grpc;


import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.grpc.echo.EchoRequest;
import org.globsframework.grpc.reader.*;
import org.globsframework.grpc.writer.*;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.nio.ByteBuffer;
import java.util.HashMap;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2, time = 3)
@Measurement(iterations = 2, time = 3)
@Fork(2)
@State(Scope.Thread)
public class PerfTest {
    private ProtobufWriter protobufWriter;
    private byte[] protobufBuffer;
    private ProtobufReader.GlobReader deserializer;
    // built once: the write benchmarks measure the encoding, not the Builder / MutableGlob construction
    private EchoRequest echoRequest;
    private Glob glob;
    // every message of echoRequest (root, sub-messages, wrappers, timestamp): each one memoizes its own size
    private Message[] allMessages;
    private static final VarHandle MEMOIZED_SIZE = memoizedSizeHandle();

    @Setup
    public void setup() throws IOException {
        protobufWriter = ProtobufWriter.Builder.init().add(ProtobufWriterImplTest.EchoRequestType.TYPE).build();
        echoRequest = ProtobufWriterImplTest.buildGrpRequest();
        glob = ProtobufWriterImplTest.buildGlobRequest();
        protobufBuffer = echoRequest.toByteArray();
        List<Message> messages = new ArrayList<>();
        collect(echoRequest, messages);
        allMessages = messages.toArray(new Message[0]);

        final ProtobufReader protobufReader = ProtobufReader.Builder.init(GlobType::instantiate)
                .add(ProtobufWriterImplTest.EchoRequestType.TYPE)
                .build();
        deserializer = protobufReader.getReader(ProtobufWriterImplTest.EchoRequestType.TYPE);
    }

    @Benchmark
    public EchoRequest testProtobufRead() throws InvalidProtocolBufferException {
        return EchoRequest.parseFrom(protobufBuffer);
    }

    @Benchmark
    public Glob testGlobRead() throws IOException {
        return deserializer.read(ProtobufWriterImplTest.EchoRequestType.TYPE, new SafeHeapReader(ByteBuffer.wrap(protobufBuffer)) );
    }

    @Benchmark
    public byte[] testProtobufWrite() {
        // a message is serialized once in real life: drop the size protobuf cached on the previous call
        for (Message message : allMessages) {
            MEMOIZED_SIZE.set(message, -1);
        }
        return echoRequest.toByteArray();
    }

    @Benchmark
    public byte[] testProtobufWriteMemoizedSize() {
        return echoRequest.toByteArray();
    }

    private static void collect(Message message, List<Message> into) {
        into.add(message);
        for (var e : message.getAllFields().entrySet()) {
            if (e.getKey().getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                if (e.getKey().isRepeated()) {
                    for (Object o : (List<?>) e.getValue()) {
                        collect((Message) o, into);
                    }
                } else {
                    collect((Message) e.getValue(), into);
                }
            }
        }
    }

    private static VarHandle memoizedSizeHandle() {
        for (Class<?> c = EchoRequest.class; c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField("memoizedSize");
                return MethodHandles.privateLookupIn(c, MethodHandles.lookup()).unreflectVarHandle(f);
            } catch (NoSuchFieldException e) {
                // look in the superclass
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("no memoizedSize in protobuf's message hierarchy");
    }

    @Benchmark
    public byte[] testGlobWrite() throws IOException {
        final BufferAllocator alloc = BufferAllocator.create();
        final BinaryWriter writer = BinaryWriter.newHeapInstance(alloc, 1024);
        protobufWriter.write(glob, writer);
        return writer.complete().array();
    }

    static {
        System.setProperty("globsframework.field.no.check", "true");
    }
}
