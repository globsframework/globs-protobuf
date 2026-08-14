package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.StringArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetStringArrayAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufStringArraySerializerImpl(int fieldNumber, GlobGetStringArrayAccessor getValueAccessor) implements ProtoBufFieldSerializer {

    public ProtoBufStringArraySerializerImpl(StringArrayField field, int fieldNumber) {
        this(fieldNumber, field.getGlobType().getGetAccessor(field));
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final String[] value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeStringList(fieldNumber, value);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final String[] value = (String[]) rawValue;
        try {
            binaryWriter.writeStringList(fieldNumber, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
