package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.GlobArrayField;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetGlobArrayAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializerImpl;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public class ProtoBufGlobArrayFieldDeserializerImpl implements ProtoBufGlobDeserializer {
    private final GlobType type;
    private final ProtoBufGlobDeserializerImpl deserializer;
    private final GlobInstantiator instantiator;
    private final GlobSetGlobArrayAccessor setAccessor;


    public ProtoBufGlobArrayFieldDeserializerImpl(GlobArrayField field, ProtoBufGlobDeserializerImpl deserializer,
                                                  GlobInstantiator instantiator) {
        this.type = field.getGlobType();
        this.deserializer = deserializer;
        this.instantiator = instantiator;
        setAccessor = field.getGlobType().getSetAccessor(field);
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readMessageList(instantiator, type, deserializer));
    }
}
