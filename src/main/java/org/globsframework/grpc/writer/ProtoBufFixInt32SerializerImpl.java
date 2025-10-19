package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.model.Glob;

import java.io.IOException;

public class ProtoBufFixInt32SerializerImpl implements ProtoBufGlobSerializer {
    private final IntegerField field;
    private final int fieldNumber;

    public ProtoBufFixInt32SerializerImpl(IntegerField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Integer value = data.get(field);
        if (value != null) {
            binaryWriter.writeFixed32(fieldNumber, value);
        }
    }
}
