package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.DoubleField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetDoubleAccessor;
import org.globsframework.grpc.reader.ProtoBufFieldDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufGlobDoubleValueFieldDeserializerImpl(GlobSetDoubleAccessor setAccessor) implements ProtoBufFieldDeserializer {

    public ProtoBufGlobDoubleValueFieldDeserializerImpl(DoubleField field) {
        this((GlobSetDoubleAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        final int previousLimit = reader.readValueHeader();
        if (previousLimit != -1) {
            final int tag = reader.getFieldNumber();
            double value = 0;
            if (tag == 1) {
                value = reader.readDouble();
            } else if (tag != Integer.MAX_VALUE) {
                throw SafeHeapReader.InvalidProtocolBufferException.parseFailure();
            }
            setAccessor.setNative(mutableGlob, value);
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
