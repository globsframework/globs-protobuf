package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;

import java.io.IOException;
import java.util.Map;

public class ProtobufWriterImpl implements ProtobufWriter {
    private final Map<GlobType, ProtoBufGlobSerializer> preInit;
    private final GlobSerializerRegistry registry;

    public ProtobufWriterImpl(GlobSerializerRegistry registry, Map<GlobType, ProtoBufGlobSerializer> preInit) {
        this.registry = registry;
        this.preInit = preInit;
    }

    @Override
    public void write(Glob data, BinaryWriter writer) throws IOException {
        if (data == null) {
            return;
        }
        final ProtoBufGlobSerializer protoBufGlobSerializer = preInit.get(data.getType());
        if (protoBufGlobSerializer != null) {
            protoBufGlobSerializer.write(data, writer);
            return;
        }
        registry.getGlobSerializer(data.getType())
                .write(data, writer);
    }

    @Override
    public GlobWriter getWriter(GlobType type) {
        ProtoBufGlobSerializer protoBufGlobSerializer = preInit.get(type);
        if (protoBufGlobSerializer == null) {
            protoBufGlobSerializer = registry.getGlobSerializer(type);
        }
        return new GlobWriterImpl(protoBufGlobSerializer);
    }

    private static class GlobWriterImpl implements GlobWriter {
        private final ProtoBufGlobSerializer protoBufGlobSerializer;

        public GlobWriterImpl(ProtoBufGlobSerializer protoBufGlobSerializer) {
            this.protoBufGlobSerializer = protoBufGlobSerializer;
        }

        @Override
        public void write(Glob data, BinaryWriter writer) throws IOException {
            protoBufGlobSerializer.write(data, writer);
        }
    }
}
