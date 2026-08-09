package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.Field;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.generate.FieldValueFunction;
import org.globsframework.core.model.generate.GenerateCaller;
import org.globsframework.core.model.generate.GeneratedFunctionCaller;
import org.globsframework.core.model.generate.GlobGenerateFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class ProtoBufGlobSerializerImpl implements ProtoBufGlobSerializer {
    private final GlobType type;
    private final ProtoBufFieldSerializer[] attributes;

    // Set by initCaller once the array is filled -- it cannot be built in the constructor, since the registry
    // publishes this instance before resolving the fields so that recursive types terminate. The class is what
    // the dispatch below tests, and it stays null when the type's factory generates nothing.
    private GeneratedFunctionCaller<BinaryWriter, Void> caller;
    private Class<?> generatedGlobClass;

    public ProtoBufGlobSerializerImpl(GlobType type, ProtoBufFieldSerializer[] fieldSerializer) {
        this.type = type;
        attributes = fieldSerializer;
    }

    /**
     * Asks the type's factory for a caller over these same leaf serializers, which is what they implement
     * FieldValueFunction for : the generated caller holds each of them in a static final field, so writing a
     * field is a monomorphic call, where the loop below is one call site for every leaf class in the process.
     * <p>
     * Nothing is asked of a type whose factory generates nothing : GenerateCaller.callerFor would answer a
     * DefaultFunctionCaller, whose loop reads through Glob.getValue rather than the typed accessor each leaf
     * holds, and calls the leaves of the fields that are not protobuf fields at all.
     * <p>
     * Must be called after the array is filled, and before the serializer is used.
     */
    void initCaller(GlobType type) {
        if (type.getGlobFactory() instanceof GlobGenerateFactory generate) {
            caller = generate.create(new GenerateCaller.GetFieldValueFunction<BinaryWriter, Void>() {
                @SuppressWarnings("unchecked")
                public <T> FieldValueFunction<T, BinaryWriter, Void> create(Field field) {
                    return (FieldValueFunction<T, BinaryWriter, Void>) attributes[field.getIndex()];
                }
            });
            // a generated caller reads the fields of its own Glob class directly, so it only accepts what that
            // type's factory built -- a Glob from a custom GlobInstantiator has to take the loop
            generatedGlobClass = type.instantiate().getClass();
        }
    }

    /**
     * Whether this type got a caller. Nothing observable depends on it — the bytes are the same either way,
     * which is exactly why a test has to be able to ask.
     */
    public boolean isCallerBased() {
        return caller != null;
    }

    public void write(Glob data, BinaryWriter writer) throws IOException {
        if (data.getType() != type) {
            throw new RuntimeException(getMessage(data));
        }
        if (data.getClass() == generatedGlobClass) {
            try {
                caller.call(data, writer, null);
            } catch (UncheckedIOException e) {
                // the leaves cannot declare IOException through FieldValueFunction, see ProtoBufFieldSerializer
                throw e.getCause();
            }
        } else {
            for (ProtoBufFieldSerializer attribute : attributes) {
                attribute.write(data, writer);
            }
        }
    }

    private String getMessage(Glob data) {
        return "Invalid type '" + data.getType() + "' expected '" + type.getName() + "'";
    }
}
