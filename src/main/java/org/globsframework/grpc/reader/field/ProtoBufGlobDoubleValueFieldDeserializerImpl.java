package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.DoubleField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetDoubleAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public final class ProtoBufGlobDoubleValueFieldDeserializerImpl implements ProtoBufGlobDeserializer {
    private final GlobSetDoubleAccessor setAccessor;

    public ProtoBufGlobDoubleValueFieldDeserializerImpl(DoubleField field) {
        setAccessor = field.getGlobType().getSetAccessor(field);
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        final int previousLimit = reader.readValueHeader();
        if (previousLimit != -1) {
            final int tag = reader.getFieldNumber();
            double value = 0;
            if (tag == 1) {
                value = reader.readDouble();
            } else if (tag != Integer.MAX_VALUE) {
                throw SafeHeapReader.InvalidProtocolBufferException.parseFailure();
            }
            setAccessor.setNative(mutableGlob, value);
            reader.endValueHeader(previousLimit);
        }
    }
}
