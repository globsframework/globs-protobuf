package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.DoubleArrayField;
import org.globsframework.core.model.Glob;

import java.io.IOException;

public class ProtoBufFloatArraySerializerImpl implements ProtoBufGlobSerializer {
    private final DoubleArrayField field;
    private final int fieldNumber;

    public ProtoBufFloatArraySerializerImpl(DoubleArrayField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        double[] values = data.get(field);
        if (values != null) {
            binaryWriter.writeFloatList(fieldNumber, values, true);
        }
    }
}
