package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.LongField;
import org.globsframework.core.model.Glob;

import java.io.IOException;

public class ProtoBufVarInt64SerializerImpl implements ProtoBufGlobSerializer {
    private final LongField field;
    private final int fieldNumber;

    public ProtoBufVarInt64SerializerImpl(LongField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Long value = data.get(field);
        if (value != null) {
            binaryWriter.writeInt64(fieldNumber, value);
        }
    }
}
