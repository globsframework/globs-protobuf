package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.*;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.grpc.ProtobufField;
import org.globsframework.grpc.reader.field.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GlobDeserializerRegistry {
    private static final Logger log = LoggerFactory.getLogger(GlobDeserializerRegistry.class);
    private final Map<GlobType, ProtoBufGlobDeserializer> deserializers;
    private final GlobInstantiator instantiator;

    public GlobDeserializerRegistry(GlobInstantiator instantiator, Map<GlobType, ProtoBufGlobDeserializer> deserializers) {
        this.instantiator = instantiator;
        this.deserializers = deserializers;
    }

    // only one thread at a time can create a deserializer due to the getDeserializer method that build desirializer
    // in two phases to managed recursion of type
    public synchronized ProtoBufGlobDeserializer getDeserializer(GlobType type) {
        final ProtoBufGlobDeserializer protoBufGlobDeserializer = deserializers.get(type);
        if (protoBufGlobDeserializer != null) {
            return protoBufGlobDeserializer;
        }
        if (log.isDebugEnabled()) {
            log.debug("Creating deserializer for {}", type.getName());
        }
        return create(type);
    }

    private ProtoBufGlobDeserializerImpl create(GlobType type) {
        ProtoBufFieldDeserializer[] attributes = new ProtoBufFieldDeserializer[computeSize(type)];
        final ProtoBufGlobDeserializerImpl value = new ProtoBufGlobDeserializerImpl(attributes);
        deserializers.put(type, value);
        createFieldDeserializer(type, attributes);

        // only now : the caller captures the deserializers, and they are only all there at this point
        value.initCaller();

        return value;
    }

    private int computeSize(GlobType type) {
        int maxLen = 0;
        for (Field field : type.getFields()) {
            final Glob annotation = field.findAnnotation(ProtobufField.KEY);
            if (annotation != null) {
                final Integer grpcNumber = annotation.getNotNull(ProtobufField.number);
                maxLen = Math.max(maxLen, grpcNumber);
            }
        }
        return maxLen + 1;
    }

    public void createFieldDeserializer(GlobType type, ProtoBufFieldDeserializer[] attributes) {
        final Field[] fields = type.getFields();
        for (Field field : fields) {
            final Glob annotation = field.findAnnotation(ProtobufField.KEY);
            if (annotation == null) {
                log.warn("'{}' is not a protobuf field (missing ProtobufField annotation)", field.getName());
                continue;
            }
            final int grpcType = annotation.get(ProtobufField.type);
            final boolean isValueType = annotation.isTrue(ProtobufField.isValue);
            final Integer grpcNumber = annotation.getNotNull(ProtobufField.number);
            if (attributes[grpcNumber] != null) {
                throw throwDuplicate(type, field, grpcNumber);
            }
            attributes[grpcNumber] =
                    switch (field) {
                        case GlobField<?> globField -> {
                            yield new ProtoBufGlobFieldDeserializerImpl(globField,
                                    getDeserializer(globField.getTargetType()), instantiator);
                        }
                        case GlobArrayField<?> globArrayField -> {
                            yield new ProtoBufGlobArrayFieldDeserializerImpl(globArrayField,
                                    getDeserializer(globArrayField.getTargetType()), instantiator);
                        }
                        case IntegerArrayField integerArrayField -> {
                            switch (grpcType) {
                                case 1, 3 -> {
                                    yield new ProtoBufGlobVarInt32ArrayDeserializerImpl(integerArrayField);
                                }
                                case 5 -> {
                                    yield new ProtoBufGlobVarSInt32ArrayDeserializerImpl(integerArrayField);
                                }
                                case 0, 12 -> {
                                    yield new ProtoBufGlobFixInt32ArrayDeserializerImpl(integerArrayField);
                                }
                                case 13 -> {
                                    yield new ProtoBufGlobVarSFix32ArrayDeserializerImpl(integerArrayField);
                                }
                                default ->
                                        throw new IllegalStateException("Unexpected value: " + grpcType + " for " + field.getFullName());
                            }
                        }
                        case LongArrayField longArrayField -> {
                            switch (grpcType) {
                                case 2, 4 -> {
                                    yield new ProtoBufGlobVarInt64ArrayDeserializerImpl(longArrayField);
                                }
                                case 6 -> {
                                    yield new ProtoBufGlobVarSInt64ArrayDeserializerImpl(longArrayField);
                                }
                                case 0, 9 -> {
                                    yield new ProtoBufGlobFixInt64ArrayDeserializerImpl(longArrayField);
                                }
                                case 10 -> {
                                    yield new ProtoBufGlobVarSFix64ArrayDeserializerImpl(longArrayField);
                                }
                                default ->
                                        throw new IllegalStateException("Unexpected value: " + grpcType + " for " + field.getFullName());
                            }
                        }
                        case IntegerField integerField -> {
                            switch (grpcType) {
                                case 0, 1, 3 -> {
                                    if (isValueType) {
                                        yield new ProtoBufGlobVarInt32ValueDeserializerImpl(integerField);
                                    } else {
                                        yield new ProtoBufGlobVarInt32DeserializerImpl(integerField);
                                    }
                                }
                                case 5 -> {
                                    yield new ProtoBufGlobVarSInt32DeserializerImpl(integerField);
                                }
                                case 8 -> {
                                    yield new ProtoBufGlobVarInt32DeserializerImpl(integerField);
                                }
                                case 12 -> {
                                    yield new ProtoBufGlobFixInt32DeserializerImpl(integerField);
                                }
                                case 13 -> {
                                    yield new ProtoBufGlobVarSFix32DeserializerImpl(integerField);
                                }
                                default ->
                                        throw new IllegalStateException("Unexpected value: " + grpcType + " for " + field.getFullName());
                            }
                        }
                        case LongField longField -> {
                            switch (grpcType) {
                                case 0, 2, 4 -> {
                                    if (isValueType) {
                                        yield new ProtoBufGlobVarInt64ValueDeserializerImpl(longField);
                                    } else {
                                        yield new ProtoBufGlobVarInt64DeserializerImpl(longField);
                                    }
                                }
                                case 6 -> {
                                    yield new ProtoBufGlobVarSInt64DeserializerImpl(longField);
                                }
                                case 9 -> {
                                    yield new ProtoBufGlobFixInt64DeserializerImpl(longField);
                                }
                                case 10 -> {
                                    yield new ProtoBufGlobVarSFix64DeserializerImpl(longField);
                                }
                                case 15 -> {
                                    yield new ProtoBufGlobLongTimestampDeserializerImpl(longField);
                                }
                                default ->
                                        throw new IllegalStateException("Unexpected value: " + grpcType + " for " + field.getFullName());
                            }
                        }
                        case BooleanField booleanField -> {
                            if (grpcType == 7 || grpcType == 0) {
                                if (isValueType) {
                                    yield new ProtoBufGlobBoolValueFieldDeserializerImpl(booleanField);
                                } else {
                                    yield new ProtoBufGlobBoolFieldDeserializerImpl(booleanField);
                                }
                            } else {
                                throw new IllegalStateException("Unexpected value: " + grpcType + " for " + field.getFullName());
                            }
                        }
                        case BooleanArrayField booleanArrayField -> {
                            if (grpcType == 7 || grpcType == 0) {
                                yield new ProtoBufGlobBoolArrayFieldDeserializerImpl(booleanArrayField);
                            } else {
                                throw new IllegalStateException("Unexpected value: " + grpcType + " for " + field.getFullName());
                            }
                        }
                        case StringField stringField -> {
                            if (grpcType == 0) {
                                if (isValueType) {
                                    yield new ProtoBufGlobStringValueDeserializerImpl(stringField);
                                } else {
                                    yield new ProtoBufGlobStringDeserializerImpl(stringField);
                                }
                            } else {
                                throw new IllegalStateException("Unexpected value: " + grpcType + " for " + field.getFullName());
                            }
                        }
                        case StringArrayField stringArrayField -> {
                            yield new ProtoBufGlobStringArrayDeserializerImpl(stringArrayField);
                        }
                        case DoubleField doubleField -> {
                            if (grpcType == 0 || grpcType == 11) {
                                if (isValueType) {
                                    yield new ProtoBufGlobDoubleValueFieldDeserializerImpl(doubleField);
                                } else {
                                    yield new ProtoBufGlobDoubleFieldDeserializerImpl(doubleField);
                                }
                            } else if (grpcType == 14) {
                                yield new ProtoBufGlobFloatFieldDeserializerImpl(doubleField);
                            } else {
                                throw new IllegalStateException("Unexpected value: " + grpcType + " for " + field.getFullName());
                            }
                        }
                        case DoubleArrayField doubleArrayField -> {
                            if (grpcType == 0 || grpcType == 11) {
                                yield new ProtoBufGlobDoubleArrayFieldDeserializerImpl(doubleArrayField);
                            } else if (grpcType == 14) {
                                yield new ProtoBufGlobFloatArrayFieldDeserializerImpl(doubleArrayField);
                            } else {
                                throw new IllegalStateException("Unexpected value: " + grpcType + " for " + field.getFullName());
                            }
                        }
                        default ->
                                throw new IllegalStateException("Unexpected value: " + field + " for " + type.getName());
                    };
        }
    }

    private static RuntimeException throwDuplicate(GlobType type, Field field, Integer grpcNumber) {
        return new RuntimeException("On field " + field.getName() + " duplicate grpc id " + grpcNumber +
                                    " with " + type.streamFields()
                                            .filter(f -> f.findOptAnnotation(ProtobufField.KEY)
                                                    .map(ProtobufField.number).orElse(-1).equals(grpcNumber)).findFirst()
                                            .orElseThrow().getFullName());
    }

}
