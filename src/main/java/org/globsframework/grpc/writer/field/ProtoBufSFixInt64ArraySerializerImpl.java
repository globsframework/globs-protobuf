package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.LongArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetLongArrayAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;

public record ProtoBufSFixInt64ArraySerializerImpl(int fieldNumber, GlobGetLongArrayAccessor getValueAccessor) implements ProtoBufFieldSerializer {

    public ProtoBufSFixInt64ArraySerializerImpl(LongArrayField field, int fieldNumber) {
        this(fieldNumber, field.getGlobType().getGetAccessor(field));
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final long[] value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeSFixed64List(fieldNumber, value, true);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter) throws IOException {
        if (isNull) {
            return;
        }
        final long[] value = (long[]) rawValue;
        binaryWriter.writeSFixed64List(fieldNumber, value, true);
    }
}
