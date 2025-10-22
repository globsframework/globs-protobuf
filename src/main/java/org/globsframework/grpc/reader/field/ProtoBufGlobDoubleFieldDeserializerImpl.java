package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.DoubleField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetDoubleAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public class ProtoBufGlobDoubleFieldDeserializerImpl implements ProtoBufGlobDeserializer {
    private final GlobSetDoubleAccessor setAccessor;

    public ProtoBufGlobDoubleFieldDeserializerImpl(DoubleField field) {
        setAccessor = field.getGlobType().getSetAccessor(field);
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.setNative(mutableGlob, reader.readDouble());
    }
}
