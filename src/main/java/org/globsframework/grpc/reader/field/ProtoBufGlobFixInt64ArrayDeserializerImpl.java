package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.LongArrayField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetLongArrayAccessor;
import org.globsframework.grpc.reader.ProtoBufFieldDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufGlobFixInt64ArrayDeserializerImpl(GlobSetLongArrayAccessor setAccessor) implements ProtoBufFieldDeserializer {

    public ProtoBufGlobFixInt64ArrayDeserializerImpl(LongArrayField field) {
        this((GlobSetLongArrayAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readFixed64List());
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
