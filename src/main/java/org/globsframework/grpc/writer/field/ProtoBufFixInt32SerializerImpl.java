package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetIntAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufGlobSerializer;

import java.io.IOException;

public class ProtoBufFixInt32SerializerImpl implements ProtoBufGlobSerializer {
    private final int fieldNumber;
    private final GlobGetIntAccessor getValueAccessor;

    public ProtoBufFixInt32SerializerImpl(IntegerField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final Integer value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeFixed32(fieldNumber, value);
        }
    }
}
