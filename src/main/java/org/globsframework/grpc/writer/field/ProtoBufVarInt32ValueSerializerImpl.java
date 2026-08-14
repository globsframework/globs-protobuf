package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetIntAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufVarInt32ValueSerializerImpl(int fieldNumber, GlobGetIntAccessor getValueAccessor) implements ProtoBufFieldSerializer {

    public ProtoBufVarInt32ValueSerializerImpl(IntegerField field, int fieldNumber) {
        this(fieldNumber, field.getGlobType().getGetAccessor(field));
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Integer value = getValueAccessor.get(data);
        if (value != null) {
            final int indexEnd = binaryWriter.getTotalBytesWritten();
            binaryWriter.writeInt32(1, value);
            binaryWriter.writeHeaderValue(fieldNumber, indexEnd);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final Integer value = (Integer) rawValue;
        try {
            final int indexEnd = binaryWriter.getTotalBytesWritten();
            binaryWriter.writeInt32(1, value);
            binaryWriter.writeHeaderValue(fieldNumber, indexEnd);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
