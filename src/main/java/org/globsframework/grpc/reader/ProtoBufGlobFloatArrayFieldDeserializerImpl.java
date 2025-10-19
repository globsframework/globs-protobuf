package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.fields.DoubleArrayField;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobFloatArrayFieldDeserializerImpl implements ProtoBufGlobDeserializer {
    private final DoubleArrayField field;

    public ProtoBufGlobFloatArrayFieldDeserializerImpl(DoubleArrayField field) {
        this.field = field;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readFloatList());
    }
}
