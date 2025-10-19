package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.BooleanField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;

import java.io.IOException;

public class ProtoBufStringSerializerImpl implements ProtoBufGlobSerializer {
    private final StringField field;
    private final int fieldNumber;

    public ProtoBufStringSerializerImpl(StringField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final String value = data.get(field);
        if (value != null) {
            binaryWriter.writeString(fieldNumber, value);
        }
    }
}
