package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.LongField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetLongAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufSFixInt64SerializerImpl(int fieldNumber, GlobGetLongAccessor getValueAccessor) implements ProtoBufFieldSerializer {

    public ProtoBufSFixInt64SerializerImpl(LongField field, int fieldNumber) {
        this(fieldNumber, field.getGlobType().getGetAccessor(field));
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Long value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeSFixed64(fieldNumber, value);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final Long value = (Long) rawValue;
        try {
            binaryWriter.writeSFixed64(fieldNumber, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
