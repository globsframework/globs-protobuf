package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.GlobArrayField;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetGlobArrayAccessor;
import org.globsframework.grpc.reader.ProtoBufFieldDeserializer;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufGlobArrayFieldDeserializerImpl(GlobType type, ProtoBufGlobDeserializer deserializer, GlobInstantiator instantiator, GlobSetGlobArrayAccessor setAccessor) implements ProtoBufFieldDeserializer {

    public ProtoBufGlobArrayFieldDeserializerImpl(GlobArrayField<?> field, ProtoBufGlobDeserializer deserializer,
                                                  GlobInstantiator instantiator) {
        this(field.getTargetType(),
                deserializer,
                instantiator,
                (GlobSetGlobArrayAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readMessageList(instantiator, type, deserializer));
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
