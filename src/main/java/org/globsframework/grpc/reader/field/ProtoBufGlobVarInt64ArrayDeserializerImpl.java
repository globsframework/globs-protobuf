package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.LongArrayField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetLongArrayAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public class ProtoBufGlobVarInt64ArrayDeserializerImpl implements ProtoBufGlobDeserializer {
    private final GlobSetLongArrayAccessor setAccessor;

    public ProtoBufGlobVarInt64ArrayDeserializerImpl(LongArrayField field) {
        setAccessor = field.getGlobType().getSetAccessor(field);
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readInt64List());
    }
}
