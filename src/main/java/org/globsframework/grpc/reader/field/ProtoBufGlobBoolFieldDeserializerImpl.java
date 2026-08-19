package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.BooleanField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetBooleanAccessor;
import org.globsframework.grpc.reader.ProtoBufFieldDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufGlobBoolFieldDeserializerImpl(GlobSetBooleanAccessor setAccessor) implements ProtoBufFieldDeserializer {

    public ProtoBufGlobBoolFieldDeserializerImpl(BooleanField field) {
        this((GlobSetBooleanAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.setNative(mutableGlob, reader.readBool());
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
