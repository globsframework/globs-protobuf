package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.LongArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetLongArrayAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufGlobSerializer;

import java.io.IOException;

public class ProtoBufFixInt64ArraySerializerImpl implements ProtoBufGlobSerializer {
    private final int fieldNumber;
    private final GlobGetLongArrayAccessor getValueAccessor;

    public ProtoBufFixInt64ArraySerializerImpl(LongArrayField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final long[] value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeFixed64List(fieldNumber, value, true);
        }
    }
}
