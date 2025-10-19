package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.fields.LongField;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobVarSInt64DeserializerImpl implements ProtoBufGlobDeserializer {
    private final LongField field;

    public ProtoBufGlobVarSInt64DeserializerImpl(LongField field) {
        this.field = field;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readSInt64());
    }
}
