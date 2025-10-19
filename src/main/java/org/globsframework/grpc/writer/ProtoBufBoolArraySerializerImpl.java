package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.BooleanArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetBooleanArrayAccessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProtoBufBoolArraySerializerImpl implements ProtoBufGlobSerializer {
    private final BooleanArrayField field;
    private final int fieldNumber;
    private final GlobGetBooleanArrayAccessor getValueAccessor;

    public ProtoBufBoolArraySerializerImpl(BooleanArrayField field, int fieldNumber) {
        this.field = field;
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
