package org.globsframework.grpc;


import org.globsframework.core.model.Glob;
import org.globsframework.grpc.echo.EchoRequest;
import org.globsframework.grpc.writer.*;
import org.openjdk.jmh.annotations.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2, time = 3)
@Measurement(iterations = 2, time = 3)
@Fork(2)
@State(Scope.Thread)
public class PerfTest {
    private GrpcBinWriter grpcBinWriter;

    @Setup
    public void setup(){
        grpcBinWriter = new ProtobufWriterImpl(new GlobSerializerRegistry());
    }

    @Benchmark
    public byte[] testProtobuf() throws IOException {
        EchoRequest echoRequest = GrpcBinWriterImplTest.buildGrpRequest();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        echoRequest.writeTo(output);
        return output.toByteArray();
    }

    @Benchmark
    public byte[] testGlob() throws IOException {
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
