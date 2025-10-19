package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobVarSInt32DeserializerImpl implements ProtoBufGlobDeserializer {
    private final IntegerField field;

    public ProtoBufGlobVarSInt32DeserializerImpl(IntegerField field) {
        this.field = field;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readSInt32());
    }
}
