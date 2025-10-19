package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.fields.DoubleField;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobFloatFieldDeserializerImpl implements ProtoBufGlobDeserializer {
    private final DoubleField field;

    public ProtoBufGlobFloatFieldDeserializerImpl(DoubleField field) {
        this.field = field;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readFloat());
    }
}
