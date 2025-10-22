package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.StringArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetStringArrayAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufGlobSerializer;

import java.io.IOException;

public class ProtoBufStringArraySerializerImpl implements ProtoBufGlobSerializer {
    private final int fieldNumber;
    private final GlobGetStringArrayAccessor getValueAccessor;

    public ProtoBufStringArraySerializerImpl(StringArrayField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = (GlobGetStringArrayAccessor) field.getGlobType().getGlobFactory().getGetValueAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final String[] value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeStringList(fieldNumber, value);
        }
    }
}
