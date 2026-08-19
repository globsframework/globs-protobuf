package org.globsframework.grpc.reader;

import org.globsframework.core.model.caller.ToGlobFunction;

/**
 * A per-field deserializer, i.e. one entry of the array {@link ProtoBufGlobDeserializerImpl} holds — as
 * opposed to the per-type composites, which are only {@link ProtoBufGlobDeserializer}s. The mirror of
 * {@link org.globsframework.grpc.writer.ProtoBufFieldSerializer}.
 * <p>
 * It can be driven two ways, and they must read the same thing. {@link #read} is called from the array,
 * indexed by the field number the tag carries; {@code call}, inherited from {@link ToGlobFunction}, is
 * what a {@code ToGlobCaller} drives — one call site per field number rather than one for the whole
 * loop, with a constant receiver, which is what makes the leaves being records worth something.
 * <p>
 * Each leaf writes {@code call} out as a one-liner over its own {@code read} rather than inheriting a default
 * here : on the exact final class that call is statically bound and free, where a default on the interface
 * would be a second interface dispatch on the path that exists to remove one (measured on the writer side,
 * 229k → 191k ops/s). {@code read} declares IOException and {@link ToGlobFunction} declares nothing, so
 * each {@code call} wraps it in UncheckedIOException and {@link ProtoBufGlobDeserializerImpl#read} unwraps it.
 */
public interface ProtoBufFieldDeserializer extends ProtoBufGlobDeserializer,
        ToGlobFunction<SafeHeapReader, Void, Void> {
}
