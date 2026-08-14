package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.LongArrayField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetLongArrayAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public record ProtoBufGlobVarSInt64ArrayDeserializerImpl(GlobSetLongArrayAccessor setAccessor) implements ProtoBufGlobDeserializer {

    public ProtoBufGlobVarSInt64ArrayDeserializerImpl(LongArrayField field) {
        this((GlobSetLongArrayAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readSInt64List());
    }
}
