package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetGlobAccessor;
import org.globsframework.grpc.reader.ProtoBufFieldDeserializer;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufGlobFieldDeserializerImpl(GlobType type, ProtoBufGlobDeserializer deserializer, GlobInstantiator instantiator, GlobSetGlobAccessor setAccessor) implements ProtoBufFieldDeserializer {

    public ProtoBufGlobFieldDeserializerImpl(GlobField<?> field, ProtoBufGlobDeserializer deserializer,
                                             GlobInstantiator instantiator) {
        this(field.getTargetType(),
                deserializer,
                instantiator,
                (GlobSetGlobAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readMessage(instantiator, type, deserializer));
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
