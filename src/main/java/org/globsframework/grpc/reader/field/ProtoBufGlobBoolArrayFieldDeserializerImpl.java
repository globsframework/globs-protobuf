package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.BooleanArrayField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetBooleanArrayAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public record ProtoBufGlobBoolArrayFieldDeserializerImpl(GlobSetBooleanArrayAccessor setAccessor) implements ProtoBufGlobDeserializer {

    public ProtoBufGlobBoolArrayFieldDeserializerImpl(BooleanArrayField field) {
        this((GlobSetBooleanArrayAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readBoolList());
    }
}
