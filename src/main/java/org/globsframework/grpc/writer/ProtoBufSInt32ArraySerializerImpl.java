package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.IntegerArrayField;
import org.globsframework.core.model.Glob;

import java.io.IOException;

public class ProtoBufSInt32ArraySerializerImpl implements ProtoBufGlobSerializer {
    private final IntegerArrayField field;
    private final int fieldNumber;

    public ProtoBufSInt32ArraySerializerImpl(IntegerArrayField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final int[] value = data.get(field);
        if (value != null) {
            binaryWriter.writeSInt32List(fieldNumber, value, true);
        }
    }
}
