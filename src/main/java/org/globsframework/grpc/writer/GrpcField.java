package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.IntegerField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.Key;
import org.globsframework.core.model.KeyBuilder;

public class GrpcField {
    public static final GlobType TYPE;

    public static final IntegerField number;

    public static final IntegerField type;

    public static final Key KEY;

    public static Glob create(int number, GrpcType type) {
        return TYPE.instantiate()
                .set(GrpcField.number, number)
                .set(GrpcField.type, type.typeID);
    }

    public static Glob create(int number) {
        return TYPE.instantiate()
                .set(GrpcField.number, number)
                .set(GrpcField.type, GrpcType.NA.typeID);
    }

    static {
        final GlobTypeBuilder grpcField = GlobTypeBuilderFactory.create("GrpcField");
        TYPE = grpcField.unCompleteType();
        number = grpcField.declareIntegerField("number");
        type = grpcField.declareIntegerField("type");
        grpcField.complete();
        KEY = KeyBuilder.newEmptyKey(TYPE);
    }


    public enum GrpcType {
        NA(0),
        int32(1), int64(2), uint32(3), uint64(4),
        sint32(5), sint64(6), bool(7), enum_(8),
        fixed64(9), sfixed64(10), double_(11),
        fixed32(12), sfixed32(13), float_(14);

        public final int typeID;

        GrpcType(int typeID) {
            this.typeID = typeID;
        }
    }
}
