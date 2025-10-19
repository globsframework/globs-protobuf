package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.*;
import org.globsframework.core.model.Glob;

import java.util.HashMap;
import java.util.Map;

public class GlobSerializerRegistry {
    private final Map<GlobType, ProtoBufGlobSerializerImpl> serializers = new HashMap<>();

    // only one thread at a time can create a deserializer due to the getDeserializer method that build desirializer
    // in two phases to managed recursion of type
    public synchronized ProtoBufGlobSerializer getGlobSerializer(GlobType type) {
        final ProtoBufGlobSerializerImpl protoBufGlobSerializer = serializers.get(type);
        if (protoBufGlobSerializer != null) {
            return protoBufGlobSerializer;
        }
        final ProtoBufGlobSerializer[] attributes = new ProtoBufGlobSerializer[type.getFieldCount()];
        final ProtoBufGlobSerializerImpl newSerializer = new ProtoBufGlobSerializerImpl(type, attributes);
        serializers.put(type, newSerializer);
        createFieldSerializer(type, attributes);
        return newSerializer;
    }

    private void createFieldSerializer(GlobType type, ProtoBufGlobSerializer[] attributes) {
        final Field[] fields = type.getFields();
        int i = 0;
        for (Field field : fields) {
            final Glob annotation = field.getAnnotation(GrpcField.KEY);
            final int grpcType = annotation.get(GrpcField.type);
            final Integer grpcNumber = annotation.getNotNull(GrpcField.number);
            attributes[i] = switch (field) {
                case LongArrayField longArrayField -> {
                    switch (grpcType) {
                        case 2, 4 -> {
                            yield new ProtoBufVarInt64ArraySerializerImpl(longArrayField, grpcNumber);
                        }
                        case 6 -> {
                            yield new ProtoBufSInt64ArraySerializerImpl(longArrayField, grpcNumber);
                        }
                        case 9 -> {
                            yield new ProtoBufFixInt64ArraySerializerImpl(longArrayField, grpcNumber);
                        }
                        case 10 -> {
                            yield new ProtoBufSFixInt64ArraySerializerImpl(longArrayField, grpcNumber);
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + grpcType);
                    }
                }
                case IntegerArrayField integerArrayField -> {
                    switch (grpcType) {
                        case 1, 3 -> {
                            yield new ProtoBufVarInt32ArraySerializerImpl(integerArrayField, grpcNumber);
                        }
                        case 5 -> {
                            yield new ProtoBufSInt32ArraySerializerImpl(integerArrayField, grpcNumber);
                        }
                        case 12 -> {
                            yield new ProtoBufFixInt32ArraySerializerImpl(integerArrayField, grpcNumber);
                        }
                        case 13 -> {
                            yield new ProtoBufSFixInt32ArraySerializerImpl(integerArrayField, grpcNumber);
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + grpcType);
                    }
                }
                case IntegerField integerField -> {
                    switch (grpcType) {
                        case 1, 3 -> {
                            yield new ProtoBufVarInt32SerializerImpl(integerField, grpcNumber);
                        }
                        case 5 -> {
                            yield new ProtoBufSInt32SerializerImpl(integerField, grpcNumber);
                        }
                        case 12 -> {
                            yield new ProtoBufFixInt32SerializerImpl(integerField, grpcNumber);
                        }
                        case 13 -> {
                            yield new ProtoBufSFixInt32SerializerImpl(integerField, grpcNumber);
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + grpcType);
                    }
                }
                case LongField longField -> {
                    switch (grpcType) {
                        case 2, 4 -> {
                            yield new ProtoBufVarInt64SerializerImpl(longField, grpcNumber);
                        }
                        case 6 -> {
                            yield new ProtoBufSInt64SerializerImpl(longField, grpcNumber);
                        }
                        case 9 -> {
                            yield new ProtoBufFixInt64SerializerImpl(longField, grpcNumber);
                        }
                        case 10 -> {
                            yield new ProtoBufSFixInt64SerializerImpl(longField, grpcNumber);
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + grpcType);
                    }
                }
                case BooleanField booleanField -> {
                    if (grpcType == 0 || grpcType == 7) {
                        yield new ProtoBufBoolSerializerImpl(booleanField, grpcNumber);
                    }
                    else {
                        throw new IllegalStateException("Unexpected value: " + grpcType);
                    }
                }
                case BooleanArrayField booleanArrayField -> {
                    if (grpcType == 0 || grpcType == 7) {
                        yield new ProtoBufBoolArraySerializerImpl(booleanArrayField, grpcNumber);
                    }
                    else {
                        throw new IllegalStateException("Unexpected value: " + grpcType);
                    }
                }
                case GlobField globField ->
                        new ProtoBufGlobFieldGlobSerializer(globField, grpcNumber, getGlobSerializer(globField.getTargetType()));
                case GlobArrayField globArrayField ->
                        new ProtoBufGlobArrayFieldGlobSerializer(globArrayField, grpcNumber, getGlobSerializer(globArrayField.getTargetType()));
                case StringField stringField ->
                    new ProtoBufStringSerializerImpl(stringField, grpcNumber);
                case StringArrayField stringArrayField ->
                    new ProtoBufStringArraySerializerImpl(stringArrayField, grpcNumber);
                case DoubleField doubleField -> {
                    if (grpcType == 0 || grpcType == 11) {
                        yield new ProtoBufDoubleSerializerImpl(doubleField, grpcNumber);
                    }
                    else if (grpcType == 14) {
                        yield new ProtoBufFloatSerializerImpl(doubleField, grpcNumber);
                    }
                    else {
                        throw new IllegalStateException("Unexpected value: " + grpcType);
                    }
                }
                case DoubleArrayField doubleArrayField -> {
                    if (grpcType == 0 || grpcType == 11) {
                        yield new ProtoBufDoubleArraySerializerImpl(doubleArrayField, grpcNumber);
                    }
                    else if (grpcType == 14) {
                        yield new ProtoBufFloatArraySerializerImpl(doubleArrayField, grpcNumber);
                    }
                    else {
                        throw new IllegalStateException("Unexpected value: " + grpcType);
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + field);
            };
            i++;
        }
    }
}
