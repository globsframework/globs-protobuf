package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetIntAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public record ProtoBufGlobVarSFix32DeserializerImpl(GlobSetIntAccessor setAccessor) implements ProtoBufGlobDeserializer {

    public ProtoBufGlobVarSFix32DeserializerImpl(IntegerField field) {
        this((GlobSetIntAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readSFixed32());
    }
}
