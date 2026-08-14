package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.DoubleField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetDoubleAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufDoubleValueSerializerImpl(int fieldNumber, GlobGetDoubleAccessor getValueAccessor) implements ProtoBufFieldSerializer {

    public ProtoBufDoubleValueSerializerImpl(DoubleField field, int fieldNumber) {
        this(fieldNumber, field.getGlobType().getGetAccessor(field));
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Double value = getValueAccessor.get(data);
        if (value != null) {
            final int indexEnd = binaryWriter.getTotalBytesWritten();
            binaryWriter.writeDouble(1, value);
            binaryWriter.writeHeaderValue(fieldNumber, indexEnd);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final Double value = (Double) rawValue;
        try {
            final int indexEnd = binaryWriter.getTotalBytesWritten();
            binaryWriter.writeDouble(1, value);
            binaryWriter.writeHeaderValue(fieldNumber, indexEnd);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
