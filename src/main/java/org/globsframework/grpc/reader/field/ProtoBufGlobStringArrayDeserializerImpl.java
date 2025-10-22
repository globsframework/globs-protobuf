package org.globsframework.grpc.reader.field;

import org.globsframework.core.metamodel.fields.StringArrayField;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.globaccessor.set.GlobSetStringArrayAccessor;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;

public class ProtoBufGlobStringArrayDeserializerImpl implements ProtoBufGlobDeserializer {
    private final GlobSetStringArrayAccessor setAccessor;

    public ProtoBufGlobStringArrayDeserializerImpl(StringArrayField field) {
        setAccessor = field.getGlobType().getSetAccessor(field);
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        setAccessor.set(mutableGlob, reader.readStringList());
    }
}
