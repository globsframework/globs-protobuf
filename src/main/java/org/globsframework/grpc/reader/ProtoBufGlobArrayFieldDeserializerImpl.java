package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.GlobArrayField;
import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobArrayFieldDeserializerImpl implements ProtoBufGlobDeserializer {
    private final GlobArrayField field;
    private final GlobType type;
    private final ProtoBufGlobDeserializerImpl deserializer;
    private final GlobInstantiator instantiator;


    public ProtoBufGlobArrayFieldDeserializerImpl(GlobArrayField field, ProtoBufGlobDeserializerImpl deserializer,
                                                  GlobInstantiator instantiator) {
        this.field = field;
        this.type = field.getGlobType();
        this.deserializer = deserializer;
        this.instantiator = instantiator;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readMessageList(instantiator, type, deserializer));
    }
}
