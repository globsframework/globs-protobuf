package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetIntAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufGlobSerializer;

import java.io.IOException;

public class ProtoBufSInt32SerializerImpl implements ProtoBufGlobSerializer {
    private final int fieldNumber;
    private final GlobGetIntAccessor getValueAccessor;

    public ProtoBufSInt32SerializerImpl(IntegerField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = (GlobGetIntAccessor) field.getGlobType().getGlobFactory().getGetValueAccessor(field);
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Integer value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeSInt32(fieldNumber, value);
        }
    }
}
