package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.DoubleArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetDoubleArrayAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;

public record ProtoBufFloatArraySerializerImpl(int fieldNumber, GlobGetDoubleArrayAccessor getValueAccessor) implements ProtoBufFieldSerializer {

    public ProtoBufFloatArraySerializerImpl(DoubleArrayField field, int fieldNumber) {
        this(fieldNumber, field.getGlobType().getGetAccessor(field));
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        double[] values = getValueAccessor.get(data);
        if (values != null) {
            binaryWriter.writeFloatList(fieldNumber, values, true);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter) throws IOException {
        if (isNull) {
            return;
        }
        final double[] values = (double[]) rawValue;
        binaryWriter.writeFloatList(fieldNumber, values, true);
    }
}
