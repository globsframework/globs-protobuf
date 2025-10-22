package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.BooleanArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetBooleanArrayAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufGlobSerializer;

import java.io.IOException;

public class ProtoBufBoolArraySerializerImpl implements ProtoBufGlobSerializer {
    private final int fieldNumber;
    private final GlobGetBooleanArrayAccessor getValueAccessor;

    public ProtoBufBoolArraySerializerImpl(BooleanArrayField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = (GlobGetBooleanArrayAccessor) field.getGlobType().getGlobFactory().getGetValueAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        boolean[] values = getValueAccessor.get(data);
        if (values != null) {
            binaryWriter.writeBoolList(fieldNumber, values, true);
        }
    }
}
