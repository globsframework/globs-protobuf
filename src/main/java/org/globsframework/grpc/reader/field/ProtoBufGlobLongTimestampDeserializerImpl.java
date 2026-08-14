package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.LongField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetLongAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public record ProtoBufGlobLongTimestampDeserializerImpl(GlobSetLongAccessor setAccessor) implements ProtoBufGlobDeserializer {

    public ProtoBufGlobLongTimestampDeserializerImpl(LongField field) {
        this((GlobSetLongAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        final int previousLimit = reader.readValueHeader();
        if (previousLimit != -1) {
            long second = 0;
            long nano = 0;
            while (true) {
                final int fieldNumber = reader.getFieldNumber();
                if (fieldNumber == 1) {
                    second = reader.readInt64();
                } else if (fieldNumber == 2) {
                    nano = reader.readInt32();
                } else if (fieldNumber == Integer.MAX_VALUE) {
                    break;
                } else {
                    throw SafeHeapReader.InvalidProtocolBufferException.parseFailure();
                };
            }
            setAccessor.setNative(mutableGlob, second * 1_000 + nano / 1_000_000);
            reader.endValueHeader(previousLimit);
        }
    }
}
