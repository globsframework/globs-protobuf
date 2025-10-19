package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.fields.LongArrayField;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobVarInt64ArrayDeserializerImpl implements ProtoBufGlobDeserializer {
    private final LongArrayField field;

    public ProtoBufGlobVarInt64ArrayDeserializerImpl(LongArrayField field) {
        this.field = field;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readInt64List());
    }
}
