package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.LongField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetLongAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufGlobSerializer;

import java.io.IOException;

public final class ProtoBufLongTimestampSerializerImpl implements ProtoBufGlobSerializer {
    private final int fieldNumber;
    private final GlobGetLongAccessor getValueAccessor;

    public ProtoBufLongTimestampSerializerImpl(LongField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Long value = getValueAccessor.get(data);
        if (value != null) {
            final int indexEnd = binaryWriter.getTotalBytesWritten();
            binaryWriter.writeInt32(2, (int) (value % 1_000) * 1_000_000);
            binaryWriter.writeInt64(1, value / 1_000);
            binaryWriter.writeHeaderValue(fieldNumber, indexEnd);
        }
    }
}
