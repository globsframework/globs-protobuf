package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetIntAccessor;
import org.globsframework.grpc.reader.ProtoBufFieldDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufGlobVarInt32ValueDeserializerImpl(GlobSetIntAccessor setAccessor) implements ProtoBufFieldDeserializer {

    public ProtoBufGlobVarInt32ValueDeserializerImpl(IntegerField field) {
        this((GlobSetIntAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        final int previousLimit = reader.readValueHeader();
        if (previousLimit != -1) {
            final int tag = reader.getFieldNumber();
            int value = 0;
            if (tag == 1) {
                value = reader.readInt32();
            } else if (tag != Integer.MAX_VALUE) {
                throw SafeHeapReader.InvalidProtocolBufferException.parseFailure();
            }
            setAccessor.set(mutableGlob, value);
            reader.endValueHeader(previousLimit);
        }
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
