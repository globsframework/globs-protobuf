package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobFieldDeserializerImpl implements ProtoBufGlobDeserializer {
    private final GlobField field;
    private final GlobType type;
    private final ProtoBufGlobDeserializerImpl deserializer;
    private final GlobInstantiator instantiator;


    public ProtoBufGlobFieldDeserializerImpl(GlobField field, ProtoBufGlobDeserializerImpl deserializer,
                                             GlobInstantiator instantiator) {
        this.field = field;
        this.type = field.getGlobType();
        this.deserializer = deserializer;
        this.instantiator = instantiator;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readMessage(instantiator, type, deserializer));
    }
}
