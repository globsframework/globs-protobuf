package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.fields.BooleanField;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobBoolFieldDeserializerImpl implements ProtoBufGlobDeserializer {
    private final BooleanField field;

    public ProtoBufGlobBoolFieldDeserializerImpl(BooleanField field) {
        this.field = field;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readBool());
    }
}
