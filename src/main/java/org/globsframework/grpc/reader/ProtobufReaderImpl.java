package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ProtobufReaderImpl implements ProtobufReader, Function<GlobType, ProtoBufGlobDeserializer> {
    private final Map<GlobType, ProtoBufGlobDeserializer> serializers = new ConcurrentHashMap<>();
    private final GlobInstantiator instantiator;
    private final GlobDeserializerRegistry globDeserializerRegistry;

    public ProtobufReaderImpl(GlobInstantiator instantiator, GlobDeserializerRegistry globDeserializerRegistry) {
        this.instantiator = instantiator;
        this.globDeserializerRegistry = globDeserializerRegistry;
    }

    public ProtobufReaderImpl(GlobInstantiator instantiator) {
        this.instantiator = instantiator;
        globDeserializerRegistry = new GlobDeserializerRegistry(instantiator);
    }


    public Glob read(GlobType type, SafeHeapReader reader) throws IOException {
        final ProtoBufGlobDeserializer protoBufGlobDeserializer =
                serializers.computeIfAbsent(type, this);

        final MutableGlob mutableGlob = instantiator.newGlob(type);

        protoBufGlobDeserializer.read(mutableGlob, reader);
        return mutableGlob;
    }

    public ProtoBufGlobDeserializer apply(GlobType type1) {
        return globDeserializerRegistry.getDeserializer(type1);
    }
}
