package org.globsframework.grpc.writer;

import org.globsframework.core.model.Glob;

import java.io.IOException;

public interface ProtoBufGlobSerializer {
    void write(Glob data, BinaryWriter writer) throws IOException;
}
