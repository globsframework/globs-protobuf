package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.DoubleField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetDoubleAccessor;

import java.io.IOException;

public class ProtoBufDoubleSerializerImpl implements ProtoBufGlobSerializer {
    private final DoubleField field;
    private final int fieldNumber;
    private final GlobGetDoubleAccessor getValueAccessor;

    public ProtoBufDoubleSerializerImpl(DoubleField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = (GlobGetDoubleAccessor) field.getGlobType().getGlobFactory().getGetValueAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Double value = (Double) getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeDouble(fieldNumber, value);
        }
    }
}
