package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.GlobInstantiator;

import java.io.IOException;

public interface ProtobufReader {
    Glob read(GlobType type, SafeHeapReader inputStream) throws IOException;

    static ProtobufReader create(GlobInstantiator instantiator, GlobDeserializerRegistry globDeserializerRegistry){
        return new ProtobufReaderImpl(instantiator, globDeserializerRegistry);
    }

    static ProtobufReader create(GlobInstantiator instantiator){
        return new ProtobufReaderImpl(instantiator);
    }
}
