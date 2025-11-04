package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.IntegerArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetIntArrayAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufGlobSerializer;

import java.io.IOException;

public class ProtoBufSInt32ArraySerializerImpl implements ProtoBufGlobSerializer {
    private final int fieldNumber;
    private final GlobGetIntArrayAccessor getValueAccessor;

    public ProtoBufSInt32ArraySerializerImpl(IntegerArrayField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final int[] value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeSInt32List(fieldNumber, value, true);
        }
    }
}
