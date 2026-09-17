package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.DoubleField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetDoubleAccessor;
import org.globsframework.grpc.reader.ProtoBufFieldDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public record ProtoBufGlobFloatFieldDeserializerImpl(GlobSetDoubleAccessor setAccessor) implements ProtoBufFieldDeserializer {

    public ProtoBufGlobFloatFieldDeserializerImpl(DoubleField field) {
        this((GlobSetDoubleAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.setNative(mutableGlob, reader.readFloat());
    }
}
