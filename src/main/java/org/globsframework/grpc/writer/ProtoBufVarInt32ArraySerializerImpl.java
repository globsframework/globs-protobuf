package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.IntegerArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetIntArrayAccessor;

import java.io.IOException;

public class ProtoBufVarInt32ArraySerializerImpl implements ProtoBufGlobSerializer {
    private final IntegerArrayField field;
    private final int fieldNumber;
    private final GlobGetIntArrayAccessor getValueAccessor;

    public ProtoBufVarInt32ArraySerializerImpl(IntegerArrayField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = (GlobGetIntArrayAccessor) field.getGlobType().getGlobFactory().getGetValueAccessor(field);
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final int[] value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeInt32List(fieldNumber, value, true);
        }
    }
}
