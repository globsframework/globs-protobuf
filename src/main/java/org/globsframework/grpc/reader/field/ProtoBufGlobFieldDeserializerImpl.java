package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetGlobAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializerImpl;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public class ProtoBufGlobFieldDeserializerImpl implements ProtoBufGlobDeserializer {
    private final GlobType type;
    private final ProtoBufGlobDeserializerImpl deserializer;
    private final GlobInstantiator instantiator;
    private final GlobSetGlobAccessor setAccessor;


    public ProtoBufGlobFieldDeserializerImpl(GlobField field, ProtoBufGlobDeserializerImpl deserializer,
                                             GlobInstantiator instantiator) {
        setAccessor = field.getGlobType().getSetAccessor(field);
        this.type = field.getGlobType();
        this.deserializer = deserializer;
        this.instantiator = instantiator;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readMessage(instantiator, type, deserializer));
    }
}
