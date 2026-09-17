package org.globsframework.grpc.writer;

import java.io.IOException;

/**
 * A per-field serializer, i.e. one entry of the array {@link ProtoBufGlobSerializerImpl} holds — as opposed to
 * the per-type composites, which are only {@link ProtoBufGlobSerializer}s.
 * <p>
 * It can be driven two ways, and they must produce the same bytes. {@link ProtoBufGlobSerializer#write} pulls
 * the value out of the Glob through the typed accessor the leaf holds; {@link #call} is handed the value
 * instead and is what a generated caller drives — one call site per field, with a constant receiver, rather
 * than the single megamorphic one of the loop. Those two, and {@link ProtoBufGlobSerializer} itself, are what
 * the caller is generated over : the emitted class is a serializer of the type and calls these directly, so
 * the writer travels as itself and the IOException with it.
 * <p>
 * Both are written out in each leaf, next to each other. Factoring the encoding into a third method that the
 * two would call does not remove a copy — {@code write} needs the accessor and the null test on the value it
 * just read, {@code call} needs neither — it only adds a hop on the path that exists to remove hops.
 * <p>
 * The one thing every {@code call} does and {@code write} does not : a null value is not written at all, set
 * or not ({@code isSet} is ignored — protobuf has no way to say "explicitly null", unlike
 * globs-bin-serialisation whose format has a NULL tag). It used to have a second : {@code call} came from
 * core's {@code FromGlobFunction}, which declared no checked exception and no {@code BinaryWriter}, so every
 * leaf wrapped its IOException into an UncheckedIOException that {@link ProtoBufGlobSerializerImpl#write}
 * unwrapped. None of that is left.
 */
public interface ProtoBufFieldSerializer extends ProtoBufGlobSerializer {

    void call(boolean isSet, boolean isNull, Object value, BinaryWriter writer) throws IOException;
}
