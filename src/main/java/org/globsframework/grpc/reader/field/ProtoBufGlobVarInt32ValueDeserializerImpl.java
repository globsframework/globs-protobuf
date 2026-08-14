package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetIntAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public record ProtoBufGlobVarInt32ValueDeserializerImpl(GlobSetIntAccessor setAccessor) implements ProtoBufGlobDeserializer {

    public ProtoBufGlobVarInt32ValueDeserializerImpl(IntegerField field) {
        this((GlobSetIntAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        final int previousLimit = reader.readValueHeader();
        if (previousLimit != -1) {
            final int tag = reader.getFieldNumber();
            int value = 0;
            if (tag == 1) {
                value = reader.readInt32();
            } else if (tag != Integer.MAX_VALUE) {
                throw SafeHeapReader.InvalidProtocolBufferException.parseFailure();
            }
            setAccessor.set(mutableGlob, value);
            reader.endValueHeader(previousLimit);
        }
    }
}
