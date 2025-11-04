package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.DoubleArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetDoubleArrayAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufGlobSerializer;

import java.io.IOException;

public class ProtoBufDoubleArraySerializerImpl implements ProtoBufGlobSerializer {
    private final int fieldNumber;
    private final GlobGetDoubleArrayAccessor getValueAccessor;

    public ProtoBufDoubleArraySerializerImpl(DoubleArrayField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        double[] values = getValueAccessor.get(data);
        if (values != null) {
            binaryWriter.writeDoubleList(fieldNumber, values, true);
        }
    }
}
