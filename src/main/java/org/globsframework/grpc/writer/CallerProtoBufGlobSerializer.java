package org.globsframework.grpc.writer;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.utils.FieldCheck;

import java.io.IOException;

/**
 * The write pass of one GlobType through the caller core generated for it, with
 * {@link ProtoBufGlobSerializerImpl}'s loop over the leaves as the fallback for a Glob that is not of the
 * class that caller reads.
 * <p>
 * Two compares per glob, and both are needed. The class is what the caller was generated over — for a
 * generated flavour one class per type, but for DefaultGlob the shared DefaultGlob32/64/128, which says
 * nothing about the type — so the type is checked as well, exactly as the loop does.
 * <p>
 * <b>{@code caller} is deliberately not final.</b> See {@code CallerGlobTypeFieldWriters} in
 * globs-bin-serialisation, whose nested-descent shape this is : a final field lets C2 fold it to a constant
 * once this serializer is inlined into its parent, devirtualize the generated caller behind it and pull its
 * whole body in at every {@code writeMessage} site, which grows the hot code for nothing. Not re-measured
 * here; the field was not final before this class existed either.
 */
public final class CallerProtoBufGlobSerializer implements ProtoBufGlobSerializer {
    private final GlobType type;
    // read on the hot path : see the class comment before making this final
    private ProtoBufGlobSerializer caller;

    public CallerProtoBufGlobSerializer(GlobType type, ProtoBufGlobSerializer caller) {
        this.type = type;
        this.caller = caller;
    }

    public void write(Glob data, BinaryWriter writer) throws IOException {
        FieldCheck.check(type, data);
        caller.write(data, writer);
    }
}
