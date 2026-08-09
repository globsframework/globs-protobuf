package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetStringAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class ProtoBufStringValueSerializerImpl implements ProtoBufFieldSerializer {
    private final int fieldNumber;
    private final GlobGetStringAccessor getValueAccessor;

    public ProtoBufStringValueSerializerImpl(StringField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final String value = getValueAccessor.get(data);
        if (value != null) {
            final int indexEnd = binaryWriter.getTotalBytesWritten();
            binaryWriter.writeString(1, value);
            binaryWriter.writeHeaderValue(fieldNumber, indexEnd);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final String value = (String) rawValue;
        try {
            final int indexEnd = binaryWriter.getTotalBytesWritten();
            binaryWriter.writeString(1, value);
            binaryWriter.writeHeaderValue(fieldNumber, indexEnd);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
