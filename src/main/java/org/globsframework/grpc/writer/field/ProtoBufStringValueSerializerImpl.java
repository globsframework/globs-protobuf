package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetStringAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufGlobSerializer;

import java.io.IOException;

public class ProtoBufStringValueSerializerImpl implements ProtoBufGlobSerializer {
    private final int fieldNumber;
    private final GlobGetStringAccessor getValueAccessor;

    public ProtoBufStringValueSerializerImpl(StringField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        final String value = getValueAccessor.get(data);
        if (value != null) {
            final int indexEnd = binaryWriter.getTotalBytesWritten();
            binaryWriter.writeString(1, value);
            binaryWriter.writeHeaderValue(fieldNumber, indexEnd);
        }
    }
}
