package org.globsframework.grpc.reader.field;

import org.globsframework.core.model.MutableGlob;
import org.globsframework.grpc.reader.ProtoBufFieldDeserializer;
import org.globsframework.grpc.reader.SafeHeapReader;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * The fallback of the generated caller : a field number this type has no deserializer for is skipped, which is
 * what the array path does for a null entry. Stateless, so a plain class — a singleton has nothing to fold.
 */
public final class SkipFieldDeserializer implements ProtoBufFieldDeserializer {
    public static final SkipFieldDeserializer INSTANCE = new SkipFieldDeserializer();

    private SkipFieldDeserializer() {
    }

    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        reader.skipField();
    }

    public void call(MutableGlob mutableGlob, SafeHeapReader reader, Void ignored, Void alsoIgnored) {
        try {
            reader.skipField();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
