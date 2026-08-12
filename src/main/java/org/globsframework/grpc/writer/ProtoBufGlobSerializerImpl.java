package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.Field;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.generate.read.FieldValueFunction;
import org.globsframework.core.model.generate.read.GenerateCaller;
import org.globsframework.core.model.generate.read.GeneratedFunctionCaller;

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
     * Asks core for a caller over these same leaf serializers, which is what they implement FieldValueFunction
     * for : a generated caller holds each of them in a static final field, so writing a field is a monomorphic
     * call, where the loop below is one call site for every leaf class in the process.
     * <p>
     * Through GenerateCaller rather than by testing GlobGenerateFactory here, so that both ways of getting one
     * reach this module : the type's own factory when the Globs are generated, and the GenerateCallerService
     * of {@code -Dglobs.caller} when they are core's DefaultGlob.
     * <p>
     * generatedCallerFor, not callerFor : null means "nobody can generate this", and the loop below is a
     * better answer than the DefaultFunctionCaller callerFor would hand back — it reads through the typed
     * accessor each leaf holds rather than Glob.getValue, and SkipFieldSerializer makes the fields that are
     * not protobuf fields free, where the caller would call them.
     * <p>
     * Must be called after the array is filled, and before the serializer is used.
     */
    void initCaller(GlobType type) {
        GeneratedFunctionCaller<BinaryWriter, Void> generated = GenerateCaller.generatedCallerFor(type,
                new GenerateCaller.GetFieldValueFunction<BinaryWriter, Void>() {
                    @SuppressWarnings("unchecked")
                    public <T> FieldValueFunction<T, BinaryWriter, Void> create(Field field) {
                        return (FieldValueFunction<T, BinaryWriter, Void>) attributes[field.getIndex()];
                    }
                });
        if (generated != null) {
            caller = generated;
            // A generated caller reads the fields of one Glob class directly -- the generated one, or the
            // concrete DefaultGlob32/64/128 -- so it only accepts what this type's factory built. write()
            // dispatches on this class, so a Glob of the right type from another source (a custom
            // GlobInstantiator) takes the loop rather than a ClassCastException.
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
