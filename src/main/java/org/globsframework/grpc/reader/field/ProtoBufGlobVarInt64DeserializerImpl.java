package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.LongField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetLongAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public record ProtoBufGlobVarInt64DeserializerImpl(GlobSetLongAccessor setAccessor) implements ProtoBufGlobDeserializer {

    public ProtoBufGlobVarInt64DeserializerImpl(LongField field) {
        this((GlobSetLongAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.setNative(mutableGlob, reader.readInt64());
    }
}
