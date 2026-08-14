package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetGlobAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public record ProtoBufGlobFieldDeserializerImpl(GlobType type, ProtoBufGlobDeserializer deserializer, GlobInstantiator instantiator, GlobSetGlobAccessor setAccessor) implements ProtoBufGlobDeserializer {

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
}
