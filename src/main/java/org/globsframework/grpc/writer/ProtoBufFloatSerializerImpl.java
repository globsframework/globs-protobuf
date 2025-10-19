package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.DoubleField;
import org.globsframework.core.model.Glob;

import java.io.IOException;

public class ProtoBufFloatSerializerImpl implements ProtoBufGlobSerializer {
    private final DoubleField field;
    private final int fieldNumber;

    public ProtoBufFloatSerializerImpl(DoubleField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Double value = data.get(field);
        if (value != null) {
            binaryWriter.writeFloat(fieldNumber, (float) value.doubleValue());
        }
    }
}
