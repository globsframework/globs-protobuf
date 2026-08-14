package org.globsframework.grpc.reader;

import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.generate.write.GeneratedCallerWrite;
import org.globsframework.core.model.generate.write.GeneratedFunctionCallerWrite;
import org.globsframework.core.model.generate.write.MutableFunctionWrite;
import org.globsframework.grpc.reader.field.SkipFieldDeserializer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.SortedMap;
import java.util.TreeMap;

public final class ProtoBufGlobDeserializerImpl implements ProtoBufGlobDeserializer {
    /** indexed by proto field number, holes and unknown numbers being skipped */
    private final ProtoBufFieldDeserializer[] attributes;

    // Set by initCaller once the array is filled -- it cannot be built in the constructor, since the registry
    // publishes this instance before resolving the fields so that recursive types terminate. Null when nothing
    // can generate one, and then the loop below is the only path.
    private GeneratedCallerWrite<SafeHeapReader, Void, Void> caller;

    public ProtoBufGlobDeserializerImpl(ProtoBufFieldDeserializer[] fieldDeserializer) {
        attributes = fieldDeserializer;
    }

    /**
     * Asks core for a caller over these deserializers, which is the whole point of them implementing
     * MutableFunctionWrite : a generated one holds each leaf in a static final field and dispatches through a
     * switch on the field number, so reading a field is a monomorphic call instead of the megamorphic one the
     * array lookup makes over every leaf class in the process. It is also what makes the leaves being records
     * worth something — a constant receiver is what lets their accessor fold.
     * <p>
     * getGenerated, not get : null means "nobody can generate this", and the array below is a better answer
     * than the looped DefaultFunctionCallerWrite, an index being cheaper than its binary search for the same
     * megamorphic call at the end. Installed with
     * {@code -Dglobs.callerWrite=org.globsframework.model.generator.AsmCallerWriteGeneratorService}, which is
     * independent of globs.builder : nothing in the emitted switch reads a Glob's layout.
     * <p>
     * Must be called after the array is filled, and before the deserializers are used.
     */
    public void initCaller() {
        GeneratedFunctionCallerWrite factory = GeneratedFunctionCallerWrite.getGenerated();
        if (factory == null) {
            return;
        }
        SortedMap<Integer, MutableFunctionWrite<SafeHeapReader, Void, Void>> functions = new TreeMap<>();
        for (int fieldNumber = 0; fieldNumber < attributes.length; fieldNumber++) {
            if (attributes[fieldNumber] != null) {
                functions.put(fieldNumber, attributes[fieldNumber]);
            }
        }
        caller = factory.create(functions, SkipFieldDeserializer.INSTANCE, Integer.MAX_VALUE);
    }

    @Override
    public void read(MutableGlob mutableGlob, SafeHeapReader reader) throws IOException {
        // one test per glob, not per field : with a caller the whole loop is the generated switch, without one
        // it is the array below, which is what runs when -Dglobs.callerWrite is unset
        if (caller != null) {
            try {
                caller.call(reader, mutableGlob, reader, null, null);
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
            return;
        }
        while (true) {
            final int tag = reader.getFieldNumber();
            if (tag == Integer.MAX_VALUE) {
                break;
            }
            if (tag < attributes.length && attributes[tag] != null) {
                attributes[tag].read(mutableGlob, reader);
            } else {
                reader.skipField();
            }
        }
    }
}
