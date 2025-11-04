package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetStringAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufGlobSerializer;

import java.io.IOException;

public class ProtoBufStringSerializerImpl implements ProtoBufGlobSerializer {
    private final int fieldNumber;
    private final GlobGetStringAccessor getValueAccessor;

    public ProtoBufStringSerializerImpl(StringField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final String value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeString(fieldNumber, value);
        }
    }
}
