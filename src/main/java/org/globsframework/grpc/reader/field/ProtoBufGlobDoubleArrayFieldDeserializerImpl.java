package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.DoubleArrayField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetDoubleArrayAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public record ProtoBufGlobDoubleArrayFieldDeserializerImpl(GlobSetDoubleArrayAccessor setAccessor) implements ProtoBufGlobDeserializer {

    public ProtoBufGlobDoubleArrayFieldDeserializerImpl(DoubleArrayField field) {
        this((GlobSetDoubleArrayAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readDoubleList());
    }
}
