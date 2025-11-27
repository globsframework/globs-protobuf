package org.globsframework.grpc.writer;

import org.globsframework.core.model.Glob;

import java.io.IOException;

public interface GrpcBinWriter {
    void write(Glob data, BinaryWriter writer) throws IOException;

    static GrpcBinWriter create(GlobSerializerRegistry registry) {
        return new ProtobufWriterImpl(registry);
    }
    static GrpcBinWriter create() {
        return new ProtobufWriterImpl();
    }
}
