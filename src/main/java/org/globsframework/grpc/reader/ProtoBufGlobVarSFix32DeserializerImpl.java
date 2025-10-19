package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobVarSFix32DeserializerImpl implements ProtoBufGlobDeserializer {
    private final IntegerField field;

    public ProtoBufGlobVarSFix32DeserializerImpl(IntegerField field) {
        this.field = field;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readSFixed32());
    }
}
