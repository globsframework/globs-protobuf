package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ProtobufWriterImpl implements GrpcBinWriter, Function<GlobType, ProtoBufGlobSerializer> {
    private final Map<GlobType, ProtoBufGlobSerializer> serializers = new ConcurrentHashMap<>();
    private final GlobSerializerRegistry registry;

    public ProtobufWriterImpl() {
        registry = new GlobSerializerRegistry();
    }

    public ProtobufWriterImpl(GlobSerializerRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void write(Glob data, BinaryWriter writer) throws IOException {
        final ProtoBufGlobSerializer globSerializer = serializers.computeIfAbsent(data.getType(), this);
        globSerializer.write(data, writer);
    }

    @Override
    public ProtoBufGlobSerializer apply(GlobType globType) {
        return registry.getGlobSerializer(globType);
    }

}
