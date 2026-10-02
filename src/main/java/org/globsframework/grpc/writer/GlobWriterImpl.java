package org.globsframework.grpc.writer;

import org.globsframework.core.model.Glob;

import java.io.IOException;

record GlobWriterImpl(ProtoBufGlobSerializer protoBufGlobSerializer) implements ProtobufWriter.GlobWriter {

    @Override
    public void write(Glob data, BinaryWriter writer) throws IOException {
        protoBufGlobSerializer.write(data, writer);
    }
}
