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
                              GlobDeserializerRegistry globDeserializerRegistry, Map<GlobType, ProtoBufGlobDeserializer> preInit) {
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
        ProtoBufGlobDeserializer protoBufGlobDeserializer = preInit.get(type);
        if (protoBufGlobDeserializer == null) {
            protoBufGlobDeserializer = globDeserializerRegistry.getDeserializer(type);
        }

        final MutableGlob mutableGlob = instantiator.newGlob(type);

        protoBufGlobDeserializer.read(mutableGlob, reader);
        return mutableGlob;
    }

    @Override
    public GlobReader getReader(GlobType type) {
        ProtoBufGlobDeserializer protoBufGlobDeserializer = preInit.get(type);
        if (protoBufGlobDeserializer == null) {
            protoBufGlobDeserializer = globDeserializerRegistry.getDeserializer(type);
        }
        return new GlobReaderImpl(protoBufGlobDeserializer, instantiator);
    }

    private static class GlobReaderImpl implements GlobReader {
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
}
