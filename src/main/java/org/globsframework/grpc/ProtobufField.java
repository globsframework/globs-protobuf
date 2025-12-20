package org.globsframework.grpc;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.BooleanField;
import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.Key;
import org.globsframework.core.model.KeyBuilder;

public class ProtobufField {
    public static final GlobType TYPE;

    public static final IntegerField number;

    public static final IntegerField type;

    public static final BooleanField isValue;

    public static final Key KEY;

    public static Glob create(int number, GrpcType type) {
        return TYPE.instantiate()
                .set(ProtobufField.number, number)
                .set(ProtobufField.type, type.typeID);
    }

    public static Glob createValue(int number, GrpcType type) {
        return TYPE.instantiate()
                .set(ProtobufField.number, number)
                .set(ProtobufField.type, type.typeID)
                .set(ProtobufField.isValue, true);
    }

    public static Glob create(int number) {
        return TYPE.instantiate()
                .set(ProtobufField.number, number)
                .set(ProtobufField.type, GrpcType.NA.typeID);
    }

    public static Glob createValue(int number) {
        return TYPE.instantiate()
                .set(ProtobufField.number, number)
                .set(ProtobufField.type, GrpcType.NA.typeID)
                .set(ProtobufField.isValue, true);
    }

    static {
        final GlobTypeBuilder grpcField = GlobTypeBuilderFactory.create("GrpcField");
        TYPE = grpcField.unCompleteType();
        number = grpcField.declareIntegerField("number");
        type = grpcField.declareIntegerField("type");
        isValue = grpcField.declareBooleanField("isValue");
        grpcField.complete();
        KEY = KeyBuilder.newEmptyKey(TYPE);
    }


    public enum GrpcType {
        NA(0),
        int32(1), int64(2), uint32(3), uint64(4),
        sint32(5), sint64(6), bool(7), enum_(8),
        fixed64(9), sfixed64(10), double_(11),
        fixed32(12), sfixed32(13), float_(14),
        timestamp(15);

        public final int typeID;

        GrpcType(int typeID) {
            this.typeID = typeID;
        }
    }
}
