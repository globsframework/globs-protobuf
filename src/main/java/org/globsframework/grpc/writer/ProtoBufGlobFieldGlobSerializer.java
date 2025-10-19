package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.model.Glob;

import java.io.IOException;

class ProtoBufGlobFieldGlobSerializer implements ProtoBufGlobSerializer {
    private final GlobField globField;
    private final Integer grpcNumber;
    private final ProtoBufGlobSerializer globSerializer;

    public ProtoBufGlobFieldGlobSerializer(GlobField globField, Integer grpcNumber,
                                           ProtoBufGlobSerializer globSerializer) {
        this.globField = globField;
        this.grpcNumber = grpcNumber;
        this.globSerializer = globSerializer;
    }

    @Override
    public void write(Glob data, BinaryWriter writer) throws IOException {
        final Glob value = data.get(globField);
        if (value != null) {
            writer.writeMessage(grpcNumber, value, globSerializer);
        }
    }
}
