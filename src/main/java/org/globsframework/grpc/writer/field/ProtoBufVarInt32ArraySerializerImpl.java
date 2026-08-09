package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.IntegerArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetIntArrayAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class ProtoBufVarInt32ArraySerializerImpl implements ProtoBufFieldSerializer {
    private final int fieldNumber;
    private final GlobGetIntArrayAccessor getValueAccessor;

    public ProtoBufVarInt32ArraySerializerImpl(IntegerArrayField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final int[] value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeInt32List(fieldNumber, value, true);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final int[] value = (int[]) rawValue;
        try {
            binaryWriter.writeInt32List(fieldNumber, value, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
