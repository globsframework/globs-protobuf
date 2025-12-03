package org.globsframework.grpc;


import com.google.protobuf.InvalidProtocolBufferException;
import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.grpc.echo.EchoRequest;
import org.globsframework.grpc.reader.*;
import org.globsframework.grpc.writer.*;
import org.openjdk.jmh.annotations.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    @Setup
    public void setup() throws IOException {
        protobufWriter = ProtobufWriter.Builder.init().add(ProtobufWriterImplTest.EchoRequestType.TYPE).build();
        EchoRequest echoRequest = ProtobufWriterImplTest.buildGrpRequest();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        echoRequest.writeTo(output);
        protobufBuffer = output.toByteArray();

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
    public byte[] testProtobufWrite() throws IOException {
        EchoRequest echoRequest = ProtobufWriterImplTest.buildGrpRequest();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        echoRequest.writeTo(output);
        return output.toByteArray();
    }

    @Benchmark
    public byte[] testGlobWrite() throws IOException {
        Glob glob = ProtobufWriterImplTest.buildGlobRequest();
        final BufferAllocator alloc = BufferAllocator.create();
        final BinaryWriter writer = BinaryWriter.newHeapInstance(alloc, 1024);
        protobufWriter.write(glob, writer);
        return writer.complete().array();
    }

    static {
        System.setProperty("globsframework.field.no.check", "true");
    }
}
