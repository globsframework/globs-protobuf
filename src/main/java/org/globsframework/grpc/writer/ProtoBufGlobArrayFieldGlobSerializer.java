package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.GlobArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetGlobAccessor;
import org.globsframework.core.model.globaccessor.get.GlobGetGlobArrayAccessor;

import java.io.IOException;

class ProtoBufGlobArrayFieldGlobSerializer implements ProtoBufGlobSerializer {
    private final GlobArrayField globField;
    private final Integer grpcNumber;
    private final ProtoBufGlobSerializer globSerializer;
    private final GlobGetGlobArrayAccessor getValueAccessor;

    public ProtoBufGlobArrayFieldGlobSerializer(GlobArrayField field, Integer grpcNumber, ProtoBufGlobSerializer globSerializer) {
        this.globField = field;
        this.grpcNumber = grpcNumber;
        this.globSerializer = globSerializer;
        this.getValueAccessor = (GlobGetGlobArrayAccessor) field.getGlobType().getGlobFactory().getGetValueAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter writer) throws IOException {
        final Glob[] value = getValueAccessor.get(data);
        if (value != null) {
            writer.writeMessageList(grpcNumber, value, globSerializer);
        }
    }
}
