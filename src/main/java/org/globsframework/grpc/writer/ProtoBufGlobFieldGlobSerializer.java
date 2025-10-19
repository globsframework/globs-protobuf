package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetGlobAccessor;
import org.globsframework.core.model.globaccessor.get.GlobGetStringArrayAccessor;

import java.io.IOException;

class ProtoBufGlobFieldGlobSerializer implements ProtoBufGlobSerializer {
    private final GlobField globField;
    private final Integer grpcNumber;
    private final ProtoBufGlobSerializer globSerializer;
    private final GlobGetGlobAccessor getValueAccessor;

    public ProtoBufGlobFieldGlobSerializer(GlobField field, Integer grpcNumber,
                                           ProtoBufGlobSerializer globSerializer) {
        this.globField = field;
        this.grpcNumber = grpcNumber;
        this.globSerializer = globSerializer;
        this.getValueAccessor = (GlobGetGlobAccessor) field.getGlobType().getGlobFactory().getGetValueAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter writer) throws IOException {
        final Glob value = getValueAccessor.get(data);
        if (value != null) {
            writer.writeMessage(grpcNumber, value, globSerializer);
        }
    }
}
