package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.DoubleField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetDoubleAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufGlobSerializer;

import java.io.IOException;

public class ProtoBufFloatSerializerImpl implements ProtoBufGlobSerializer {
    private final int fieldNumber;
    private final GlobGetDoubleAccessor getValueAccessor;

    public ProtoBufFloatSerializerImpl(DoubleField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Double value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeFloat(fieldNumber, (float) value.doubleValue());
        }
    }
}
