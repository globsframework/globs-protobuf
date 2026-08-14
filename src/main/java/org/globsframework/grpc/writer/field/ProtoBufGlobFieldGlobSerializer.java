package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetGlobAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;
import org.globsframework.grpc.writer.ProtoBufGlobSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public record ProtoBufGlobFieldGlobSerializer(int grpcNumber, ProtoBufGlobSerializer globSerializer, GlobGetGlobAccessor getValueAccessor) implements ProtoBufFieldSerializer {

    public ProtoBufGlobFieldGlobSerializer(GlobField<?> field, int grpcNumber,
                                           ProtoBufGlobSerializer globSerializer) {
        this(grpcNumber, globSerializer, field.getGlobType().getGetAccessor(field));
    }

    @Override
    public void write(Glob data, BinaryWriter writer) throws IOException {
        final Glob value = getValueAccessor.get(data);
        if (value != null) {
            writer.writeMessage(grpcNumber, value, globSerializer);
        }
    }

    public void call(boolean isSet, boolean isNull, Object rawValue, BinaryWriter writer, Void ignored) {
        if (isNull) {
            return;
        }
        final Glob value = (Glob) rawValue;
        try {
            writer.writeMessage(grpcNumber, value, globSerializer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
