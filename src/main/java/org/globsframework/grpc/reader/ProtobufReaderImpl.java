package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtobufReaderImpl implements ProtobufReader {
    private final Map<GlobType, ProtoBufGlobDeserializer> preInit;
    private final GlobInstantiator instantiator;
    private final GlobDeserializerRegistry globDeserializerRegistry;

    public ProtobufReaderImpl(GlobInstantiator instantiator,
                              GlobDeserializerRegistry globDeserializerRegistry,
                              Map<GlobType, ProtoBufGlobDeserializer> preInit) {
        this.instantiator = instantiator;
        this.globDeserializerRegistry = globDeserializerRegistry;
        this.preInit = preInit;
    }

    public ProtobufReaderImpl(GlobInstantiator instantiator) {
        this.instantiator = instantiator;
        preInit = new ConcurrentHashMap<>();
        globDeserializerRegistry = new GlobDeserializerRegistry(instantiator, preInit);
    }

    public Glob read(GlobType type, SafeHeapReader reader) throws IOException {
        ProtoBufGlobDeserializer protoBufGlobDeserializer = getDeserializer(type);
        final MutableGlob mutableGlob = instantiator.newGlob(type);
        protoBufGlobDeserializer.read(mutableGlob, reader);
        return mutableGlob;
    }

    @Override
    public GlobReader getReader(GlobType type) {
        ProtoBufGlobDeserializer protoBufGlobDeserializer = getDeserializer(type);
        return new GlobReaderImpl(protoBufGlobDeserializer, instantiator);
    }

    private ProtoBufGlobDeserializer getDeserializer(GlobType type) {
        ProtoBufGlobDeserializer protoBufGlobDeserializer = preInit.get(type);
        if (protoBufGlobDeserializer == null) {
            protoBufGlobDeserializer = globDeserializerRegistry.getDeserializer(type);
        }
        return protoBufGlobDeserializer;
    }
}
