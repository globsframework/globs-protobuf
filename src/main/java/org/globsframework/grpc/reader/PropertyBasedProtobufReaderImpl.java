package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.core.model.MutableGlob;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PropertyBasedProtobufReaderImpl implements ProtobufReader, GlobType.Build<ProtoBufGlobDeserializer> {
    private static final GlobType.Property<ProtoBufGlobDeserializer> prop = new GlobType.Property<>();
    private final GlobInstantiator instantiator;
    private final GlobDeserializerRegistry globDeserializerRegistry;

    public PropertyBasedProtobufReaderImpl(GlobInstantiator instantiator,
                                           GlobDeserializerRegistry globDeserializerRegistry,
                                           Map<GlobType, ProtoBufGlobDeserializer> preInit){
        this.instantiator = instantiator;
        this.globDeserializerRegistry = globDeserializerRegistry;
        for (Map.Entry<GlobType, ProtoBufGlobDeserializer> globTypeProtoBufGlobDeserializerEntry : preInit.entrySet()) {
            globTypeProtoBufGlobDeserializerEntry.getKey().init(prop, globTypeProtoBufGlobDeserializerEntry.getValue());
        }
    }

    public Glob read(GlobType type, SafeHeapReader reader) throws IOException {
        final MutableGlob mutableGlob = instantiator.newGlob(type);
        type.get(prop, this).read(mutableGlob, reader);
        return mutableGlob;
    }

    @Override
    public GlobReader getReader(GlobType type) {
        final ProtoBufGlobDeserializer protoBufGlobDeserializer = type.get(prop, this);
        return new GlobReaderImpl(protoBufGlobDeserializer, instantiator);
    }

    @Override
    public ProtoBufGlobDeserializer create(GlobType globType) {
        return globDeserializerRegistry.getDeserializer(globType);
    }
}
