package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.utils.FieldCheck;

import java.io.IOException;

public final class ProtoBufGlobSerializerImpl implements ProtoBufGlobSerializer {
    private final GlobType type;

    /** indexed by field declaration index, and walked in that order */
    private final ProtoBufFieldSerializer[] attributes;

    public ProtoBufGlobSerializerImpl(GlobType type, ProtoBufFieldSerializer[] fieldSerializer) {
        this.type = type;
        attributes = fieldSerializer;
    }

    public void write(Glob data, BinaryWriter writer) throws IOException {
        FieldCheck.check(type, data);
        for (ProtoBufFieldSerializer attribute : attributes) {
            attribute.write(data, writer);
        }
    }
}
