package org.globsframework.grpc.reader;

import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public class ProtoBufGlobDeserializerImpl implements ProtoBufGlobDeserializer {
    private final ProtoBufGlobDeserializer[] attributes;

    public ProtoBufGlobDeserializerImpl(ProtoBufGlobDeserializer[] fieldDeserializer) {
        attributes = fieldDeserializer;
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        while (true) {
            final int tag = reader.getFieldNumber();
            if (tag == Integer.MAX_VALUE) {
                break;
            }
            if (tag < attributes.length && attributes[tag] != null) {
                attributes[tag].read(mutableGlob, reader);
            } else {
                reader.skipField();
            }
        }
    }
}
