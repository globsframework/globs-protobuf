package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.DoubleArrayField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetDoubleArrayAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public class ProtoBufGlobFloatArrayFieldDeserializerImpl implements ProtoBufGlobDeserializer {
    private final GlobSetDoubleArrayAccessor setAccessor;

    public ProtoBufGlobFloatArrayFieldDeserializerImpl(DoubleArrayField field) {
        setAccessor = field.getGlobType().getSetAccessor(field);
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readFloatList());
    }
}
