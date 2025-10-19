package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.LongField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetLongAccessor;

import java.io.IOException;

public class ProtoBufSFixInt64SerializerImpl implements ProtoBufGlobSerializer {
    private final LongField field;
    private final int fieldNumber;
    private final GlobGetLongAccessor getValueAccessor;

    public ProtoBufSFixInt64SerializerImpl(LongField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = (GlobGetLongAccessor) field.getGlobType().getGlobFactory().getGetValueAccessor(field);
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Long value = (Long) getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeSFixed64(fieldNumber, value);
        }
    }
}
