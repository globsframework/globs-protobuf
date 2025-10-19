package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.GlobArrayField;
import org.globsframework.core.model.Glob;

import java.io.IOException;

class ProtoBufGlobArrayFieldGlobSerializer implements ProtoBufGlobSerializer {
    private final GlobArrayField globField;
    private final Integer grpcNumber;
    private final ProtoBufGlobSerializer globSerializer;

    public ProtoBufGlobArrayFieldGlobSerializer(GlobArrayField globField, Integer grpcNumber, ProtoBufGlobSerializer globSerializer) {
        this.globField = globField;
        this.grpcNumber = grpcNumber;
        this.globSerializer = globSerializer;
    }

    @Override
    public void write(Glob data, BinaryWriter writer) throws IOException {
        final Glob[] value = data.get(globField);
        if (value != null) {
            writer.writeMessageList(grpcNumber, value, globSerializer);
        }
    }
}
