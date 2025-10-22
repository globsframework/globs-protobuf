package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.BooleanField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetBooleanAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public class ProtoBufGlobBoolFieldDeserializerImpl implements ProtoBufGlobDeserializer {
    private final GlobSetBooleanAccessor setAccessor;

    public ProtoBufGlobBoolFieldDeserializerImpl(BooleanField field) {
        setAccessor = field.getGlobType().getSetAccessor(field);
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.setNative(mutableGlob, reader.readBool());
    }
}
