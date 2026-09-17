package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.BooleanArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetBooleanArrayAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;

public record ProtoBufBoolArraySerializerImpl(int fieldNumber, GlobGetBooleanArrayAccessor getValueAccessor) implements ProtoBufFieldSerializer {

    public ProtoBufBoolArraySerializerImpl(BooleanArrayField field, int fieldNumber) {
        this(fieldNumber, field.getGlobType().getGetAccessor(field));
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        boolean[] values = getValueAccessor.get(data);
        if (values != null) {
            binaryWriter.writeBoolList(fieldNumber, values, true);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter) throws IOException {
        if (isNull) {
            return;
        }
        final boolean[] values = (boolean[]) rawValue;
        binaryWriter.writeBoolList(fieldNumber, values, true);
    }
}
