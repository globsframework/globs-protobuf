package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface ProtobufWriter {
    void write(Glob data, BinaryWriter writer) throws IOException;

    GlobWriter getWriter(GlobType type);

    interface GlobWriter {
        void write(Glob data, BinaryWriter writer) throws IOException;
    }

    class Builder {
        private final GlobSerializerRegistry registry;
        private final HashMap<GlobType, ProtoBufGlobSerializer> serializers;

        private Builder() {
            serializers = new HashMap<>();
            this.registry = new GlobSerializerRegistry(serializers);
        }

        public static Builder init() {
            return new Builder();
        }

        public Builder add(GlobType globType) {
            registry.getGlobSerializer(globType);
            return this;
        }

        public Builder add(Iterable<GlobType> globTypes) {
            globTypes.forEach(this::add);
            return this;
        }

        public ProtobufWriter build() {
            return new ProtobufWriterImpl(registry, serializers.isEmpty() ? Map.of() : new HashMap<>(serializers));
        }
    }

    static ProtobufWriter create() {
        final ConcurrentHashMap<GlobType, ProtoBufGlobSerializer> serializers = new ConcurrentHashMap<>();
        return new ProtobufWriterImpl(new GlobSerializerRegistry(serializers), serializers);
    }

}
