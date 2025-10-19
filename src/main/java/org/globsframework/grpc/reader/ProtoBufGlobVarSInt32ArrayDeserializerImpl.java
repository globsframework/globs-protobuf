package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.fields.IntegerArrayField;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobVarSInt32ArrayDeserializerImpl implements ProtoBufGlobDeserializer {
    private final IntegerArrayField field;

    public ProtoBufGlobVarSInt32ArrayDeserializerImpl(IntegerArrayField field) {
        this.field = field;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readSInt32List());
    }
}
