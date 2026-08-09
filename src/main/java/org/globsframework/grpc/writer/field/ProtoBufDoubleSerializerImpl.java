package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.DoubleField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetDoubleAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class ProtoBufDoubleSerializerImpl implements ProtoBufFieldSerializer {
    private final int fieldNumber;
    private final GlobGetDoubleAccessor getValueAccessor;

    public ProtoBufDoubleSerializerImpl(DoubleField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Double value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeDouble(fieldNumber, value);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final Double value = (Double) rawValue;
        try {
            binaryWriter.writeDouble(fieldNumber, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
