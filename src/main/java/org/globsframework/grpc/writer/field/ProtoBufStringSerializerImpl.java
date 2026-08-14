package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetStringAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufStringSerializerImpl(int fieldNumber, GlobGetStringAccessor getValueAccessor) implements ProtoBufFieldSerializer {

    public ProtoBufStringSerializerImpl(StringField field, int fieldNumber) {
        this(fieldNumber, field.getGlobType().getGetAccessor(field));
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final String value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeString(fieldNumber, value);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final String value = (String) rawValue;
        try {
            binaryWriter.writeString(fieldNumber, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
