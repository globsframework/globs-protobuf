package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtobufReaderImpl implements ProtobufReader {
    private final Map<GlobType, ProtoBufGlobDeserializer> serializers = new ConcurrentHashMap<>();
    private final GlobInstantiator instantiator;
    private final GlobDeserializerRegistry globDeserializerRegistry;

    public ProtobufReaderImpl(GlobInstantiator instantiator) {
        this.instantiator = instantiator;
        globDeserializerRegistry = new GlobDeserializerRegistry(instantiator);
    }


    public Glob read(GlobType type, SafeHeapReader reader) throws IOException {
        final ProtoBufGlobDeserializer protoBufGlobDeserializer =
                serializers.computeIfAbsent(type, globDeserializerRegistry::getDeserializer);

        final MutableGlob mutableGlob = instantiator.newGlob(type);

        protoBufGlobDeserializer.read(mutableGlob, reader);
        return mutableGlob;
    }
}
