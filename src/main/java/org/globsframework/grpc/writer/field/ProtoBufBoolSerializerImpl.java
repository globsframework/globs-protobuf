package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.BooleanField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetBooleanAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;

public record ProtoBufBoolSerializerImpl(int fieldNumber, GlobGetBooleanAccessor getValueAccessor) implements ProtoBufFieldSerializer {

    public ProtoBufBoolSerializerImpl(BooleanField field, int fieldNumber) {
        this(fieldNumber, field.getGlobType().getGetAccessor(field));
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Boolean value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeBool(fieldNumber, value);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter) throws IOException {
        if (isNull) {
            return;
        }
        final Boolean value = (Boolean) rawValue;
        binaryWriter.writeBool(fieldNumber, value);
    }
}
