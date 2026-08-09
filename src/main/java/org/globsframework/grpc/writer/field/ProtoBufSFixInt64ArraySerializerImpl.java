package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.LongArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetLongArrayAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class ProtoBufSFixInt64ArraySerializerImpl implements ProtoBufFieldSerializer {
    private final int fieldNumber;
    private final GlobGetLongArrayAccessor getValueAccessor;

    public ProtoBufSFixInt64ArraySerializerImpl(LongArrayField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final long[] value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeSFixed64List(fieldNumber, value, true);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final long[] value = (long[]) rawValue;
        try {
            binaryWriter.writeSFixed64List(fieldNumber, value, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
