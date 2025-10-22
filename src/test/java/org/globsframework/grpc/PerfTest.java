package org.globsframework.grpc;


import com.google.protobuf.InvalidProtocolBufferException;
import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.grpc.echo.EchoRequest;
import org.globsframework.grpc.reader.GlobDeserializerRegistry;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializerImpl;
import org.globsframework.grpc.reader.SafeHeapReader;
import org.globsframework.grpc.writer.*;
import org.openjdk.jmh.annotations.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2, time = 3)
@Measurement(iterations = 2, time = 3)
@Fork(2)
@State(Scope.Thread)
public class PerfTest {
    private GrpcBinWriter grpcBinWriter;
    private byte[] protobufBuffer;
    private ProtoBufGlobDeserializerImpl deserializer;

    @Setup
    public void setup() throws IOException {
        grpcBinWriter = new ProtobufWriterImpl(new GlobSerializerRegistry());
        EchoRequest echoRequest = GrpcBinWriterImplTest.buildGrpRequest();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        echoRequest.writeTo(output);
        protobufBuffer = output.toByteArray();

        GlobDeserializerRegistry globDeserializerRegistry = new GlobDeserializerRegistry(GlobType::instantiate);
        deserializer = globDeserializerRegistry.getDeserializer(GrpcBinWriterImplTest.EchoRequestType.TYPE);
    }

    @Benchmark
    public EchoRequest testProtobufRead() throws InvalidProtocolBufferException {
        return EchoRequest.parseFrom(protobufBuffer);
    }

    @Benchmark
    public Glob testGlobRead() throws IOException {
        final MutableGlob instantiate = GrpcBinWriterImplTest.EchoRequestType.TYPE.instantiate();
        deserializer.read(instantiate, new SafeHeapReader(ByteBuffer.wrap(protobufBuffer), true) );
        return instantiate;
    }

    @Benchmark
    public byte[] testProtobufWrite() throws IOException {
        EchoRequest echoRequest = GrpcBinWriterImplTest.buildGrpRequest();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        echoRequest.writeTo(output);
        return output.toByteArray();
    }

    @Benchmark
    public byte[] testGlobWrite() throws IOException {
        Glob glob = GrpcBinWriterImplTest.buildGlobRequest();
        final BufferAllocator alloc = new BufferAllocator();
        final BinaryWriter writer = BinaryWriter.newHeapInstance(alloc, grpcBinWriter, 1024);
        grpcBinWriter.write(glob, writer);
        return writer.complete().element().array();
    }

    static {
        System.setProperty("globsframework.field.no.check", "true");
    }
}
