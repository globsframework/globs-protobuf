package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.IntegerArrayField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetIntArrayAccessor;
import org.globsframework.grpc.reader.ProtoBufFieldDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufGlobFixInt32ArrayDeserializerImpl(GlobSetIntArrayAccessor setAccessor) implements ProtoBufFieldDeserializer {

    public ProtoBufGlobFixInt32ArrayDeserializerImpl(IntegerArrayField field) {
        this((GlobSetIntArrayAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readFixed32List());
    }

    /** The same read, driven by a GeneratedCallerWrite : one call site per field number. */
    public void call(MutableGlob mutableGlob, SafeHeapReader reader, Void ignored, Void alsoIgnored) {
        try {
            read(mutableGlob, reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
