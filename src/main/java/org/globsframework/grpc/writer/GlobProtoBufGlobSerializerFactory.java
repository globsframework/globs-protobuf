package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.caller.FromGlobCallerFactory;

public class GlobProtoBufGlobSerializerFactory {
    /** what a generated caller is emitted over : both of these are ours, so nothing is adapted */
    private static final Class<?>[] ARGUMENTS = {BinaryWriter.class};

    /**
     * Asks core for a caller over these same leaf serializers, which is what they implement
     * {@link ProtoBufFieldSerializer} for : a generated caller holds each of them in a static final field, so
     * writing a field is a monomorphic call, where the loop of {@link ProtoBufGlobSerializerImpl} is one call
     * site for every leaf class in the process.
     * <p>
     * Through FromGlobCallerFactory rather than by testing CallerGlobFactory here, so that both ways of getting
     * one reach this module : the type's own factory when the Globs are generated, and the FromGlobCallerService
     * of {@code -Dglobs.caller.fromGlob} when they are core's DefaultGlob.
     * <p>
     * generatedCallerFor, not callerFor : null means "nobody can generate this", and the loop is a better
     * answer than the looped caller callerFor would hand back — it reads through the typed accessor each leaf
     * holds rather than Glob.getValue, and SkipFieldSerializer makes the fields that are not protobuf fields
     * free, where the caller would call them. Since the shape became ours, that fallback is a reflective Proxy
     * on top, which only widens the gap.
     * <p>
     * The two interfaces it is emitted over are {@link ProtoBufGlobSerializer} — whose {@code write} is
     * already {@code (Glob, BinaryWriter)}, the shape core asks for — and {@link ProtoBufFieldSerializer}.
     * Both ours, so the emitted class <em>is</em> a serializer of this type and hands each leaf the writer as
     * itself, IOException included.
     * <p>
     * The name is the identity of the emitted class, and here the purpose alone is enough : unlike the
     * to-Glob side, {@code generatedCallerFor} adds the type it is generating over. Constant in the source,
     * which is what makes that class the same one from one run to the next.
     * <p>
     * Must be called after the array is filled, and before the serializer is used.
     */
    public static ProtoBufGlobSerializer create(GlobType type, ProtoBufFieldSerializer[] attributes) {
        ProtoBufGlobSerializer generated = FromGlobCallerFactory.generatedCallerFor("grpc.write", type,
                field -> attributes[field.getIndex()], null, ProtoBufGlobSerializer.class,
                ProtoBufFieldSerializer.class, ARGUMENTS);
        if (generated != null) {
            return new CallerProtoBufGlobSerializer(type, generated);
        } else {
            return new ProtoBufGlobSerializerImpl(type, attributes);
        }
    }
}
