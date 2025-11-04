package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.BooleanField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetBooleanAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufGlobSerializer;

import java.io.IOException;

public class ProtoBufBoolSerializerImpl implements ProtoBufGlobSerializer {
    private final int fieldNumber;
    private final GlobGetBooleanAccessor getValueAccessor;

    public ProtoBufBoolSerializerImpl(BooleanField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Boolean value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeBool(fieldNumber, value);
        }
    }
}
