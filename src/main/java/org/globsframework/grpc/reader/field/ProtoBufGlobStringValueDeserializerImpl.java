package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetStringAccessor;
import org.globsframework.grpc.reader.ProtoBufFieldDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufGlobStringValueDeserializerImpl(GlobSetStringAccessor setAccessor) implements ProtoBufFieldDeserializer {

    public ProtoBufGlobStringValueDeserializerImpl(StringField field) {
        this((GlobSetStringAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        final int previousLimit = reader.readValueHeader();
        if (previousLimit != -1) {
            final int tag = reader.getFieldNumber();
            String value = "";
            if (tag == 1) {
                value = reader.readString();
            } else if (tag != Integer.MAX_VALUE) {
                throw SafeHeapReader.InvalidProtocolBufferException.parseFailure();
            }
            setAccessor.set(mutableGlob, value);
            reader.endValueHeader(previousLimit);
        }
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
