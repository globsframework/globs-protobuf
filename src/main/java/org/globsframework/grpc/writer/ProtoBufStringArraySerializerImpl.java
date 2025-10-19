package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.fields.StringArrayField;
import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetStringAccessor;
import org.globsframework.core.model.globaccessor.get.GlobGetStringArrayAccessor;

import java.io.IOException;

public class ProtoBufStringArraySerializerImpl implements ProtoBufGlobSerializer {
    private final StringArrayField field;
    private final int fieldNumber;
    private final GlobGetStringArrayAccessor getValueAccessor;

    public ProtoBufStringArraySerializerImpl(StringArrayField field, int fieldNumber) {
        this.field = field;
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
