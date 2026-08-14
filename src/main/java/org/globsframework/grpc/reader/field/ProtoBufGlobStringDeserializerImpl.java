package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetStringAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public record ProtoBufGlobStringDeserializerImpl(GlobSetStringAccessor setAccessor) implements ProtoBufGlobDeserializer {

    public ProtoBufGlobStringDeserializerImpl(StringField field) {
        this((GlobSetStringAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readString());
    }
}
