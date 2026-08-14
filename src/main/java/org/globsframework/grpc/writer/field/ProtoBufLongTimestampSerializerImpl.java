package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.LongField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetLongAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufLongTimestampSerializerImpl(int fieldNumber, GlobGetLongAccessor getValueAccessor) implements ProtoBufFieldSerializer {

    public ProtoBufLongTimestampSerializerImpl(LongField field, int fieldNumber) {
        this(fieldNumber, field.getGlobType().getGetAccessor(field));
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Long value = getValueAccessor.get(data);
        if (value != null) {
            final int indexEnd = binaryWriter.getTotalBytesWritten();
            binaryWriter.writeInt32(2, (int) (value % 1_000) * 1_000_000);
            binaryWriter.writeInt64(1, value / 1_000);
            binaryWriter.writeHeaderValue(fieldNumber, indexEnd);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final Long value = (Long) rawValue;
        try {
            final int indexEnd = binaryWriter.getTotalBytesWritten();
            binaryWriter.writeInt32(2, (int) (value % 1_000) * 1_000_000);
            binaryWriter.writeInt64(1, value / 1_000);
            binaryWriter.writeHeaderValue(fieldNumber, indexEnd);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
