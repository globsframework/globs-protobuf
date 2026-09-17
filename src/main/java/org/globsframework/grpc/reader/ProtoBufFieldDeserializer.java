package org.globsframework.grpc.reader;

/**
 * A per-field deserializer, i.e. one entry of the array {@link ProtoBufGlobDeserializerImpl} holds — as
 * opposed to the per-type composites, which are only {@link ProtoBufGlobDeserializer}s. The mirror of
 * {@link org.globsframework.grpc.writer.ProtoBufFieldSerializer}.
 * <p>
 * It adds nothing to {@link ProtoBufGlobDeserializer} : what it says is <em>which</em> of the two roles an
 * implementation plays, and that is what the generated caller is built over — the emitted class implements
 * {@link ProtoBufGlobDeserializer} and calls these, both of them ours, so the leaf's own
 * {@code read(MutableGlob, SafeHeapReader) throws IOException} is what the switch calls, with its own
 * descriptor and its own exception.
 * <p>
 * It used to carry a second method as well, {@code call}, inherited from core's {@code ToGlobFunction} : the
 * caller was generated over three {@code Object} contexts, two of which were always null here, so every leaf
 * needed a one-liner wrapping its {@code read} and turning the IOException into an UncheckedIOException that
 * {@link ProtoBufGlobDeserializerImpl#read} unwrapped. None of that is left.
 */
public interface ProtoBufFieldDeserializer extends ProtoBufGlobDeserializer {
}
