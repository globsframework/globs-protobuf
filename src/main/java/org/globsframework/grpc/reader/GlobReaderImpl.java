package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;

final class GlobReaderImpl implements ProtobufReader.GlobReader {
    private final ProtoBufGlobDeserializer protoBufGlobDeserializer;
    private final GlobInstantiator instantiator;

    public GlobReaderImpl(ProtoBufGlobDeserializer protoBufGlobDeserializer, GlobInstantiator instantiator) {
        this.protoBufGlobDeserializer = protoBufGlobDeserializer;
        this.instantiator = instantiator;
    }

    @Override
    public Glob read(GlobType type, SafeHeapReader inputStream) throws IOException {
        final MutableGlob instantiate = instantiator.newGlob(type);
        protoBufGlobDeserializer.read(instantiate, inputStream);
        return instantiate;

    }
}
