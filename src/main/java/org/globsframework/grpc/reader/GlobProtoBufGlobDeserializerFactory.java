package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.core.model.caller.ToGlobCallerFactory;
import org.globsframework.grpc.reader.field.SkipFieldDeserializer;

import java.util.SortedMap;
import java.util.TreeMap;

public class GlobProtoBufGlobDeserializerFactory {
    private static final Class<?>[] ARGUMENTS = {MutableGlob.class, SafeHeapReader.class};

    /**
     * Asks core for a caller over these deserializers : a generated one holds each leaf in a static final
     * field and dispatches through a switch on the field number, so reading a field is a monomorphic call
     * instead of the megamorphic one the array lookup makes over every leaf class in the process. It is also
     * what makes the leaves being records worth something — a constant receiver is what lets their accessor
     * fold.
     * <p>
     * The two interfaces it is emitted over are {@link ProtoBufGlobDeserializer} and
     * {@link ProtoBufFieldDeserializer}, ours both, so the emitted class <em>is</em> a deserializer of this
     * type and calls each leaf's own {@code read} — its descriptor, its IOException, no wrapper. The reader
     * is also the {@code KeySource} the loop asks for the next field number, which is why it is simply one of
     * the two arguments.
     * <p>
     * generated, not get : null means "nobody can generate this", and the array below is a better answer
     * than the looped LoopToGlobCallerFactory, an index being cheaper than its binary search for the same
     * megamorphic call at the end. Installed with
     * {@code -Dglobs.caller.toGlob=org.globsframework.model.generator.AsmCallerWriteGeneratorService}, which is
     * independent of globs.builder : nothing in the emitted switch reads a Glob's layout.
     * <p>
     * The name is the identity of the emitted class, and it has to carry the type : a write-side caller is
     * built from functions alone, so nothing else here tells one type's deserializers from another's.
     * Constant for a given type, which is what makes the generated class the same one from one run to the
     * next.
     * <p>
     * Must be called after the array is filled, and before the deserializers are used.
     */

    public static ProtoBufGlobDeserializer create(GlobType type, ProtoBufFieldDeserializer[] attributes) {
        ToGlobCallerFactory factory = ToGlobCallerFactory.generated();
        if (factory != null) {
            SortedMap<Integer, ProtoBufFieldDeserializer> functions = new TreeMap<>();
            for (int fieldNumber = 0; fieldNumber < attributes.length; fieldNumber++) {
                if (attributes[fieldNumber] != null) {
                    functions.put(fieldNumber, attributes[fieldNumber]);
                }
            }

            return factory.create("grpc.read." + type.getName(), functions, SkipFieldDeserializer.INSTANCE,
                    Integer.MAX_VALUE, ProtoBufGlobDeserializer.class, ProtoBufFieldDeserializer.class,
                    ARGUMENTS);
        } else {
            return new ProtoBufGlobDeserializerImpl(attributes);
        }
    }
}
