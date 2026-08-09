package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.LongField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetLongAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class ProtoBufFixInt64SerializerImpl implements ProtoBufFieldSerializer {
    private final int fieldNumber;
    private final GlobGetLongAccessor getValueAccessor;

    public ProtoBufFixInt64SerializerImpl(LongField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Long value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeFixed64(fieldNumber, value);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final Long value = (Long) rawValue;
        try {
            binaryWriter.writeFixed64(fieldNumber, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
