package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.BooleanField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetBooleanAccessor;

import java.io.IOException;

public class ProtoBufBoolSerializerImpl implements ProtoBufGlobSerializer {
    private final BooleanField field;
    private final int fieldNumber;
    private final GlobGetBooleanAccessor getValueAccessor;

    public ProtoBufBoolSerializerImpl(BooleanField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = (GlobGetBooleanAccessor) field.getGlobType().getGlobFactory().getGetValueAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Boolean value = (Boolean) getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeBool(fieldNumber, value);
        }
    }
}
