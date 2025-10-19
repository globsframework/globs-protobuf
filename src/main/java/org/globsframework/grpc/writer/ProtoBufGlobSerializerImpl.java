package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;

import java.io.IOException;

public class ProtoBufGlobSerializerImpl implements ProtoBufGlobSerializer {
    private final GlobType type;
    private final ProtoBufGlobSerializer[] attributes;

    public ProtoBufGlobSerializerImpl(GlobType type, ProtoBufGlobSerializer[] fieldSerializer) {
        this.type = type;
        attributes = fieldSerializer;
    }

    public void write(Glob data, BinaryWriter writer) throws IOException {
        if (data.getType() != type) {
            throw new RuntimeException("Invalid type " + data.getType() + " expected " + type);
        }
        for (ProtoBufGlobSerializer attribute : attributes) {
            attribute.write(data, writer);
        }
    }
}
