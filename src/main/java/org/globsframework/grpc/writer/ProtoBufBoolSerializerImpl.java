package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.BooleanField;
import org.globsframework.core.model.Glob;

import java.io.IOException;

public class ProtoBufBoolSerializerImpl implements ProtoBufGlobSerializer {
    private final BooleanField field;
    private final int fieldNumber;

    public ProtoBufBoolSerializerImpl(BooleanField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Boolean value = data.get(field);
        if (value != null) {
            binaryWriter.writeBool(fieldNumber, value);
        }
    }
}
