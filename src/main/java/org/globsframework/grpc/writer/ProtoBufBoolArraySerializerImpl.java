package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.BooleanArrayField;
import org.globsframework.core.model.Glob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProtoBufBoolArraySerializerImpl implements ProtoBufGlobSerializer {
    private final BooleanArrayField field;
    private final int fieldNumber;

    public ProtoBufBoolArraySerializerImpl(BooleanArrayField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        boolean[] values = data.get(field);
        if (values != null) {
            binaryWriter.writeBoolList(fieldNumber, values, true);
        }
    }
}
