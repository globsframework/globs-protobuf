package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetIntAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;

public record ProtoBufSInt32SerializerImpl(int fieldNumber, GlobGetIntAccessor getValueAccessor) implements ProtoBufFieldSerializer {

    public ProtoBufSInt32SerializerImpl(IntegerField field, int fieldNumber) {
        this(fieldNumber, field.getGlobType().getGetAccessor(field));
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Integer value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeSInt32(fieldNumber, value);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter) throws IOException {
        if (isNull) {
            return;
        }
        final Integer value = (Integer) rawValue;
        binaryWriter.writeSInt32(fieldNumber, value);
    }
}
