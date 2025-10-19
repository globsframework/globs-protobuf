package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetIntAccessor;

import java.io.IOException;

public class ProtoBufFixInt32SerializerImpl implements ProtoBufGlobSerializer {
    private final IntegerField field;
    private final int fieldNumber;
    private final GlobGetIntAccessor getValueAccessor;

    public ProtoBufFixInt32SerializerImpl(IntegerField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = (GlobGetIntAccessor) field.getGlobType().getGlobFactory().getGetValueAccessor(field);
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Integer value = (Integer) getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeFixed32(fieldNumber, value);
        }
    }
}
