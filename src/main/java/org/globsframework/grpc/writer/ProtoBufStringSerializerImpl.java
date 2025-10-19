package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetStringAccessor;

import java.io.IOException;

public class ProtoBufStringSerializerImpl implements ProtoBufGlobSerializer {
    private final StringField field;
    private final int fieldNumber;
    private final GlobGetStringAccessor getValueAccessor;

    public ProtoBufStringSerializerImpl(StringField field, int fieldNumber) {
        this.field = field;
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = (GlobGetStringAccessor) field.getGlobType().getGlobFactory().getGetValueAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final String value = getValueAccessor.get(data);
        if (value != null) {
            binaryWriter.writeString(fieldNumber, value);
        }
    }
}
