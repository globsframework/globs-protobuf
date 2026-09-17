package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetIntAccessor;
import org.globsframework.grpc.reader.ProtoBufFieldDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public record ProtoBufGlobFixInt32DeserializerImpl(GlobSetIntAccessor setAccessor) implements ProtoBufFieldDeserializer {

    public ProtoBufGlobFixInt32DeserializerImpl(IntegerField field) {
        this((GlobSetIntAccessor) field.getGlobType().getSetAccessor(field));
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.setNative(mutableGlob, reader.readFixed32());
    }
}
