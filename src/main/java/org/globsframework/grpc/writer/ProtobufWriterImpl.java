package org.globsframework.grpc.writer;

import org.globsframework.core.model.Glob;

import java.io.IOException;

public class ProtobufWriterImpl implements GrpcBinWriter {
    private final GlobSerializerRegistry registry;

    public ProtobufWriterImpl(GlobSerializerRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void write(Glob data, BinaryWriter writer) throws IOException {
        final ProtoBufGlobSerializer globSerializer = registry.getGlobSerializer(data.getType());
        globSerializer.write(data, writer);
    }
}
