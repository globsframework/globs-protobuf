package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.IntegerArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetIntArrayAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufSFixInt32ArraySerializerImpl(int fieldNumber, GlobGetIntArrayAccessor getValueAccessor) implements ProtoBufFieldSerializer {

    public ProtoBufSFixInt32ArraySerializerImpl(IntegerArrayField field, int fieldNumber) {
        this(fieldNumber, field.getGlobType().getGetAccessor(field));
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final int[] value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeSFixed32List(fieldNumber, value, true);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final int[] value = (int[]) rawValue;
        try {
            binaryWriter.writeSFixed32List(fieldNumber, value, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
