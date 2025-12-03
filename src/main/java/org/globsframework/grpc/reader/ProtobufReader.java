package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.GlobInstantiator;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface ProtobufReader {
    Glob read(GlobType type, SafeHeapReader inputStream) throws IOException;

    GlobReader getReader(GlobType type);

    interface GlobReader {
        Glob read(GlobType type, SafeHeapReader inputStream) throws IOException;
    }

     class Builder {
         private final Map<GlobType, ProtoBufGlobDeserializer> deserializers = new HashMap<>();
         private final GlobDeserializerRegistry globDeserializerRegistry;
         private final GlobInstantiator instantiator;

         public Builder(GlobInstantiator instantiator) {
             globDeserializerRegistry = new GlobDeserializerRegistry(instantiator, deserializers);
             this.instantiator = instantiator;
         }

         public static Builder init(GlobInstantiator instantiator) {
             return new Builder(instantiator);
         }

         public Builder add(GlobType globType) {
             globDeserializerRegistry.getDeserializer(globType);
             return this;
         }

         public Builder add(Iterable<GlobType> globTypes) {
             globTypes.forEach(this::add);
             return this;
         }

         public ProtobufReader build() {
             return new ProtobufReaderImpl(instantiator, globDeserializerRegistry,
                     deserializers.isEmpty() ? Map.of() : new HashMap<>(deserializers));
         }
    }

    static ProtobufReader create(GlobInstantiator instantiator){
        final ConcurrentHashMap<GlobType, ProtoBufGlobDeserializer> deserializers = new ConcurrentHashMap<>();
        return new ProtobufReaderImpl(instantiator, new GlobDeserializerRegistry(instantiator, deserializers), deserializers);
    }
}
