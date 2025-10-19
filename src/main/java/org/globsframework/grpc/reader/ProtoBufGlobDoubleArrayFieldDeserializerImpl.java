package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.fields.DoubleArrayField;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobDoubleArrayFieldDeserializerImpl implements ProtoBufGlobDeserializer {
    private final DoubleArrayField field;

    public ProtoBufGlobDoubleArrayFieldDeserializerImpl(DoubleArrayField field) {
        this.field = field;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readDoubleList());
    }
}
