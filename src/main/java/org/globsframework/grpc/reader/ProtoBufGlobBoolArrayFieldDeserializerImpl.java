package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.fields.BooleanArrayField;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProtoBufGlobBoolArrayFieldDeserializerImpl implements ProtoBufGlobDeserializer {
    private final BooleanArrayField field;

    public ProtoBufGlobBoolArrayFieldDeserializerImpl(BooleanArrayField field) {
        this.field = field;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        mutableGlob.set(field, reader.readBoolList());
    }
}
