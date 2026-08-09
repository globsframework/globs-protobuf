package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.DoubleArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetDoubleArrayAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class ProtoBufDoubleArraySerializerImpl implements ProtoBufFieldSerializer {
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

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final double[] values = (double[]) rawValue;
        try {
            binaryWriter.writeDoubleList(fieldNumber, values, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
