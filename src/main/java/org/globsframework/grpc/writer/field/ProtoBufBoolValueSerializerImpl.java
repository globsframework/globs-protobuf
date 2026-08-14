package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.BooleanField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetBooleanAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufBoolValueSerializerImpl(int fieldNumber, GlobGetBooleanAccessor getValueAccessor) implements ProtoBufFieldSerializer {

    public ProtoBufBoolValueSerializerImpl(BooleanField field, int fieldNumber) {
        this(fieldNumber, field.getGlobType().getGetAccessor(field));
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Boolean value = getValueAccessor.get(data);
        if (value != null) {
            final int indexEnd = binaryWriter.getTotalBytesWritten();
            binaryWriter.writeBool(1, value);
            binaryWriter.writeHeaderValue(fieldNumber, indexEnd);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final Boolean value = (Boolean) rawValue;
        try {
            final int indexEnd = binaryWriter.getTotalBytesWritten();
            binaryWriter.writeBool(1, value);
            binaryWriter.writeHeaderValue(fieldNumber, indexEnd);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
