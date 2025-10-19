package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.LongArrayField;
import org.globsframework.core.model.Glob;

import java.io.IOException;

public class ProtoBufSInt64ArraySerializerImpl implements ProtoBufGlobSerializer {
    private final LongArrayField field;
    private final int fieldNumber;

    public ProtoBufSInt64ArraySerializerImpl(LongArrayField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final long[] value = data.get(field);
        if (value != null) {
            binaryWriter.writeSInt64List(fieldNumber, value, true);
        }
    }
}
