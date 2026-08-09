package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.DoubleArrayField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetDoubleArrayAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class ProtoBufFloatArraySerializerImpl implements ProtoBufFieldSerializer {
    private final int fieldNumber;
    private final GlobGetDoubleArrayAccessor getValueAccessor;

    public ProtoBufFloatArraySerializerImpl(DoubleArrayField field, int fieldNumber) {
        this.fieldNumber = fieldNumber;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
    }

    @Override
    public void write(Glob data, BinaryWriter binaryWriter) throws IOException {
        double[] values = getValueAccessor.get(data);
        if (values != null) {
            binaryWriter.writeFloatList(fieldNumber, values, true);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter binaryWriter, Void ignored) {
        if (isNull) {
            return;
        }
        final double[] values = (double[]) rawValue;
        try {
            binaryWriter.writeFloatList(fieldNumber, values, true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
