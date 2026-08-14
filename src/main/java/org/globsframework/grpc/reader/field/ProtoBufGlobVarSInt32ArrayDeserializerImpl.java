package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.IntegerArrayField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetIntArrayAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public record ProtoBufGlobVarSInt32ArrayDeserializerImpl(GlobSetIntArrayAccessor setAccessor) implements ProtoBufGlobDeserializer {

    public ProtoBufGlobVarSInt32ArrayDeserializerImpl(IntegerArrayField field) {
        this((GlobSetIntArrayAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readSInt32List());
    }
}
