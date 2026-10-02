package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.grpc.reader.ProtoBufGlobDeserializer;

import java.io.IOException;
import java.util.Map;

public final class PropertyBasedProtobufWriterImpl implements ProtobufWriter, GlobType.Build<ProtoBufGlobSerializer> {
    private static final GlobType.Property<ProtoBufGlobSerializer> prop = new GlobType.Property<>();
    private final GlobSerializerRegistry registry;

    public PropertyBasedProtobufWriterImpl(GlobSerializerRegistry registry, Map<GlobType, ProtoBufGlobSerializer> preInit) {
        this.registry = registry;
        for (Map.Entry<GlobType, ProtoBufGlobSerializer> globTypeProtoBufGlobSerializerEntry : preInit.entrySet()) {
            globTypeProtoBufGlobSerializerEntry.getKey().init(prop, globTypeProtoBufGlobSerializerEntry.getValue());
        }
    }

    @Override
    public void write(Glob data, BinaryWriter writer) throws IOException {
        if (data == null) {
            return;
        }
        final ProtoBufGlobSerializer protoBufGlobSerializer = data.getType().get(prop, this);
        protoBufGlobSerializer.write(data, writer);
    }

    @Override
    public GlobWriter getWriter(GlobType type) {
        final ProtoBufGlobSerializer protoBufGlobSerializer = type.get(prop, this);
        return new GlobWriterImpl(protoBufGlobSerializer);
    }

    @Override
    public ProtoBufGlobSerializer create(GlobType globType) {
        return registry.getGlobSerializer(globType);
    }
}
