package org.globsframework.grpc.writer.field;

import org.globsframework.core.model.Glob;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.ProtoBufFieldSerializer;

/**
 * The entry of a field with no {@code ProtobufField} annotation : nothing is written, whatever it holds.
 * Was a lambda, which a {@link ProtoBufFieldSerializer} cannot be — it has two methods.
 */
public final class SkipFieldSerializer implements ProtoBufFieldSerializer {
    public static final SkipFieldSerializer INSTANCE = new SkipFieldSerializer();

    private SkipFieldSerializer() {
    }

    public void write(Glob data, BinaryWriter writer) {
    }

    public void call(boolean isSet, boolean isNull, Object value, BinaryWriter writer, Void ignored) {
    }
}
