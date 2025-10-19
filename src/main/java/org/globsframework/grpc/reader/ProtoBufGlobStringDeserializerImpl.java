package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.fields.StringField;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobStringDeserializerImpl implements ProtoBufGlobDeserializer {
    private final StringField field;

    public ProtoBufGlobStringDeserializerImpl(StringField field) {
        this.field = field;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readString());
    }
}
