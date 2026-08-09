package org.globsframework.grpc.writer;

import org.globsframework.core.model.generate.FieldValueFunction;

/**
 * A per-field serializer, i.e. one entry of the array {@link ProtoBufGlobSerializerImpl} holds — as opposed to
 * the per-type composites, which are only {@link ProtoBufGlobSerializer}s.
 * <p>
 * It can be driven two ways, and they must produce the same bytes. {@link #write} pulls the value out of the
 * Glob through the typed accessor the leaf holds; {@code call}, inherited from {@link FieldValueFunction}, is
 * handed the value instead and is what a {@code GeneratedFunctionCaller} drives — one call site per field,
 * with a constant receiver, rather than the single megamorphic one of the loop.
 * <p>
 * Both are written out in each leaf, next to each other. Factoring the encoding into a third method that the
 * two would call does not remove a copy — {@code write} needs the accessor and the null test on the value it
 * just read, {@code call} needs neither — it only adds a hop on the path that exists to remove hops.
 * <p>
 * Two things every {@code call} does and {@code write} does not : a null value is not written at all, set or
 * not ({@code isSet} is ignored — protobuf has no way to say "explicitly null", unlike globs-bin-serialisation
 * whose format has a NULL tag), and {@code IOException} is wrapped in {@code UncheckedIOException} because
 * {@link FieldValueFunction} declares no checked exception. {@link ProtoBufGlobSerializerImpl#write} unwraps it.
 */
public interface ProtoBufFieldSerializer extends ProtoBufGlobSerializer,
        FieldValueFunction<Object, BinaryWriter, Void> {
}
