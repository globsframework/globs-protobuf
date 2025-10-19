package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.fields.DoubleField;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobDoubleFieldDeserializerImpl implements ProtoBufGlobDeserializer {
    private final DoubleField field;

    public ProtoBufGlobDoubleFieldDeserializerImpl(DoubleField field) {
        this.field = field;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readDouble());
    }
}
