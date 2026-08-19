package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetStringAccessor;
import org.globsframework.grpc.reader.ProtoBufFieldDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufGlobStringDeserializerImpl(GlobSetStringAccessor setAccessor) implements ProtoBufFieldDeserializer {

    public ProtoBufGlobStringDeserializerImpl(StringField field) {
        this((GlobSetStringAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readString());
    }

    /** The same read, driven by a ToGlobCaller : one call site per field number. */
    public void call(MutableGlob mutableGlob, SafeHeapReader reader, Void ignored, Void alsoIgnored) {
        try {
            read(mutableGlob, reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
