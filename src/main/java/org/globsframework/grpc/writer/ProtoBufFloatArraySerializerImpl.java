package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.DoubleArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetDoubleArrayAccessor;

import java.io.IOException;

public class ProtoBufFloatArraySerializerImpl implements ProtoBufGlobSerializer {
    private final DoubleArrayField field;
    private final int fieldNumber;
    private final GlobGetDoubleArrayAccessor getValueAccessor;

    public ProtoBufFloatArraySerializerImpl(DoubleArrayField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = (GlobGetDoubleArrayAccessor) field.getGlobType().getGlobFactory().getGetValueAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        double[] values = getValueAccessor.get(data);
        if (values != null) {
            binaryWriter.writeFloatList(fieldNumber, values, true);
        }
    }
}
