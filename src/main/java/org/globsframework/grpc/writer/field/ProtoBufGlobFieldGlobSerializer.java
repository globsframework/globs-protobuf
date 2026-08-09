package org.globsframework.grpc.writer.field;

import org.globsframework.core.metamodel.fields.GlobField;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.globaccessor.get.GlobGetGlobAccessor;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;
import org.globsframework.grpc.writer.ProtoBufGlobSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class ProtoBufGlobFieldGlobSerializer implements ProtoBufFieldSerializer {
    private final Integer grpcNumber;
    private final ProtoBufGlobSerializer globSerializer;
    private final GlobGetGlobAccessor getValueAccessor;

    public ProtoBufGlobFieldGlobSerializer(GlobField<?> field, Integer grpcNumber,
                                           ProtoBufGlobSerializer globSerializer) {
        this.grpcNumber = grpcNumber;
        this.globSerializer = globSerializer;
        this.getValueAccessor = field.getGlobType().getGetAccessor(field);
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
