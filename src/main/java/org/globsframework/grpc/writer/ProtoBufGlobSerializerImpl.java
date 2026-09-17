package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.fields.Field;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.caller.FromGlobCallerFactory;

import java.io.IOException;

public final class ProtoBufGlobSerializerImpl implements ProtoBufGlobSerializer {
    private final GlobType type;
    /** what a generated caller is emitted over : both of these are ours, so nothing is adapted */
    private static final Class<?>[] ARGUMENTS = {BinaryWriter.class};

    private final ProtoBufFieldSerializer[] attributes;

    // Set by initCaller once the array is filled -- it cannot be built in the constructor, since the registry
    // publishes this instance before resolving the fields so that recursive types terminate. The class is what
    // the dispatch below tests, and it stays null when the type's factory generates nothing.
    private ProtoBufGlobSerializer caller;
    private Class<?> generatedGlobClass;

    public ProtoBufGlobSerializerImpl(GlobType type, ProtoBufFieldSerializer[] fieldSerializer) {
        this.type = type;
        attributes = fieldSerializer;
    }

    /**
     * Asks core for a caller over these same leaf serializers, which is what they implement FromGlobFunction
     * for : a generated caller holds each of them in a static final field, so writing a field is a monomorphic
     * call, where the loop below is one call site for every leaf class in the process.
     * <p>
     * Through FromGlobCallerFactory rather than by testing CallerGlobFactory here, so that both ways of getting one
     * reach this module : the type's own factory when the Globs are generated, and the FromGlobCallerService
     * of {@code -Dglobs.caller.fromGlob} when they are core's DefaultGlob.
     * <p>
     * generatedCallerFor, not callerFor : null means "nobody can generate this", and the loop below is a
     * better answer than the looped caller callerFor would hand back — it reads through the typed
     * accessor each leaf holds rather than Glob.getValue, and SkipFieldSerializer makes the fields that are
     * not protobuf fields free, where the caller would call them. Since the shape became ours, that fallback
     * is a reflective Proxy on top, which only widens the gap.
     * <p>
     * The two interfaces it is emitted over are {@link ProtoBufGlobSerializer} — whose {@code write} is
     * already {@code (Glob, BinaryWriter)}, the shape core asks for — and {@link ProtoBufFieldSerializer}.
     * Both ours, so the emitted class <em>is</em> a serializer of this type and hands each leaf the writer as
     * itself, IOException included.
     * <p>
     * Must be called after the array is filled, and before the serializer is used.
     */
    void initCaller(GlobType type) {
        // the name is the identity of the emitted class : the purpose only, since generatedCallerFor adds
        // the type it is generating over
        ProtoBufGlobSerializer generated = FromGlobCallerFactory.generatedCallerFor("grpc.write", type,
                field -> attributes[field.getIndex()], null, ProtoBufGlobSerializer.class,
                ProtoBufFieldSerializer.class, ARGUMENTS);
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
            caller.write(data, writer);
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
