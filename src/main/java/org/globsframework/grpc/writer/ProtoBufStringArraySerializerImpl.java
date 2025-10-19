package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.StringArrayField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;

import java.io.IOException;

public class ProtoBufStringArraySerializerImpl implements ProtoBufGlobSerializer {
    private final StringArrayField field;
    private final int fieldNumber;

    public ProtoBufStringArraySerializerImpl(StringArrayField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final String[] value = data.get(field);
        if (value != null) {
            binaryWriter.writeStringList(fieldNumber, value);
        }
    }
}
