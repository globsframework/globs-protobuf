package org.globsframework.grpc.reader;

import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

public interface ProtoBufGlobDeserializer {

    void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException;
}
