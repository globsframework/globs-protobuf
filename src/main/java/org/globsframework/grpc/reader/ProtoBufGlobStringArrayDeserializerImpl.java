package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.fields.StringArrayField;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobStringArrayDeserializerImpl implements ProtoBufGlobDeserializer {
    private final StringArrayField field;

    public ProtoBufGlobStringArrayDeserializerImpl(StringArrayField field) {
        this.field = field;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readStringList());
    }
}
