package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.IntegerArrayField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetIntArrayAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public class ProtoBufGlobVarInt32ArrayDeserializerImpl implements ProtoBufGlobDeserializer {
    private final GlobSetIntArrayAccessor setAccessor;

    public ProtoBufGlobVarInt32ArrayDeserializerImpl(IntegerArrayField field) {
        setAccessor = field.getGlobType().getSetAccessor(field);
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readInt32List());
    }
}
