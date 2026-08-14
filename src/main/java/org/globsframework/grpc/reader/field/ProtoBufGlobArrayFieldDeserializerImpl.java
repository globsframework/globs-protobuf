package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.GlobArrayField;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetGlobArrayAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public record ProtoBufGlobArrayFieldDeserializerImpl(GlobType type, ProtoBufGlobDeserializer deserializer, GlobInstantiator instantiator, GlobSetGlobArrayAccessor setAccessor) implements ProtoBufGlobDeserializer {

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
}
