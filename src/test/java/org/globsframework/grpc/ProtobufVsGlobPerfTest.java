package org.globsframework.grpc;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import org.globsframework.core.model.Glob;
import org.globsframework.grpc.perf.Shape0;
import org.globsframework.grpc.perf.Shape1;
import org.globsframework.grpc.perf.Shape2;
import org.globsframework.grpc.perf.Shape3;
import org.globsframework.grpc.reader.SafeHeapReader;
import org.globsframework.grpc.writer.AllocatedBuffer;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.BufferAllocator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

/**
 * Globs (core's DefaultGlob) against protobuf-java's generated classes, over {@link PerfTypeFamily}'s four
 * different types — the same four shapes, field for field, in {@code perf_shapes.proto} — so that neither side
 * enjoys the monomorphic call sites of a one-type benchmark ({@link PerfTest} is that one).
 * <p>
 * Three ways to look at the write side:
 * <ul>
 *     <li>{@code *BuildAndWrite} — the object is built, then serialized, each time: what a server answering a
 *     request does. Both build through typed calls ({@link PerfShapes}).</li>
 *     <li>{@code *Write} — the objects are built once and only serialized. protobuf's messages memoize their
 *     serialized size; it is reset before each write, a message being serialized once in real life.</li>
 *     <li>{@code protobufWriteMemoizedSize} — the same, keeping protobuf's cached size, for reference.</li>
 * </ul>
 * Both reads parse the bytes protobuf-java wrote. {@code caller} is {@link CallerMode}: it only changes the Globs
 * side, the protobuf arms run under both values and say how stable the machine was.
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(3)
@State(Scope.Thread)
public class ProtobufVsGlobPerfTest {

    @Param({"OFF", "ON"})
    public String caller;

    private PerfTypeFamily family;
    private IntFunction<Glob>[] globBuilders;
    private IntFunction<Message>[] protoBuilders;
    private Parser<?>[] parsers;
    private Glob[] globs;
    private Message[] messages;
    private Message[][] allMessages;
    private byte[][] encoded;

    private static final VarHandle MEMOIZED_SIZE = memoizedSizeHandle();

    @SuppressWarnings("unchecked")
    @Setup
    public void setup() throws IOException {
        family = CallerMode.valueOf(caller).build(() -> PerfTypeFamily.create(GlobFlavour.DEFAULT));
        final PerfShapes.Glob0 g0 = new PerfShapes.Glob0(family.types[0]);
        final PerfShapes.Glob1 g1 = new PerfShapes.Glob1(family.types[1]);
        final PerfShapes.Glob2 g2 = new PerfShapes.Glob2(family.types[2]);
        final PerfShapes.Glob3 g3 = new PerfShapes.Glob3(family.types[3]);
        globBuilders = new IntFunction[]{s -> g0.build(s, true), s -> g1.build(s, true),
                s -> g2.build(s, true), s -> g3.build(s, true)};
        protoBuilders = new IntFunction[]{s -> PerfShapes.proto0(s, true), s -> PerfShapes.proto1(s, true),
                s -> PerfShapes.proto2(s, true), s -> PerfShapes.proto3(s, true)};
        parsers = new Parser[]{Shape0.parser(), Shape1.parser(), Shape2.parser(), Shape3.parser()};

        final int n = family.types.length;
        globs = new Glob[n];
        messages = new Message[n];
        allMessages = new Message[n][];
        encoded = new byte[n][];
        for (int i = 0; i < n; i++) {
            globs[i] = globBuilders[i].apply(i);
            messages[i] = protoBuilders[i].apply(i);
            final List<Message> all = new ArrayList<>();
            collect(messages[i], all);
            allMessages[i] = all.toArray(new Message[0]);
            encoded[i] = messages[i].toByteArray();

            // both sides must hold the same data, or the comparison means nothing
            if (!parsers[i].parseFrom(globWrite(globs[i])).equals(messages[i])
                    || !java.util.Arrays.equals(globWrite(globs[i]), globWrite(globs[i], reusingAllocator))) {
                throw new IllegalStateException("shape " + i + ": the Glob does not encode to the protobuf message");
            }
            // compared through protobuf: proto3 does not put a false / 0 on the wire, so the Glob read back
            // has those fields unset where the original had them set
            if (!parsers[i].parseFrom(globWrite(globRead(i))).equals(messages[i])) {
                throw new IllegalStateException("shape " + i + ": protobuf's bytes do not read back to the Glob");
            }
            if (!PerfTypeFamily.describe(globs[i]).equals(PerfTypeFamily.describe(family.data[i]))) {
                throw new IllegalStateException("shape " + i + ": PerfShapes and PerfTypeFamily.fill disagree");
            }
        }
    }

    // ---- read

    @Benchmark
    public void globRead(Blackhole bh) throws IOException {
        for (int i = 0; i < encoded.length; i++) {
            bh.consume(globRead(i));
        }
    }

    @Benchmark
    public void protobufRead(Blackhole bh) throws IOException {
        for (int i = 0; i < encoded.length; i++) {
            bh.consume(parsers[i].parseFrom(encoded[i]));
        }
    }

    // ---- write, objects built once

    @Benchmark
    public void globWrite(Blackhole bh) throws IOException {
        for (Glob glob : globs) {
            bh.consume(globWrite(glob));
        }
    }

    /** same as globWrite, with an allocator that hands the same chunks back on every write */
    @Benchmark
    public void globWriteReusedBuffer(Blackhole bh) throws IOException {
        for (Glob glob : globs) {
            bh.consume(globWrite(glob, reusingAllocator));
        }
    }

    @Benchmark
    public void protobufWrite(Blackhole bh) {
        for (int i = 0; i < messages.length; i++) {
            for (Message message : allMessages[i]) {
                MEMOIZED_SIZE.set(message, -1);
            }
            bh.consume(messages[i].toByteArray());
        }
    }

    @Benchmark
    public void protobufWriteMemoizedSize(Blackhole bh) {
        for (Message message : messages) {
            bh.consume(message.toByteArray());
        }
    }

    // ---- write, object rebuilt each time

    @Benchmark
    public void globBuildAndWrite(Blackhole bh) throws IOException {
        for (int i = 0; i < globBuilders.length; i++) {
            bh.consume(globWrite(globBuilders[i].apply(i)));
        }
    }

    @Benchmark
    public void globBuildAndWriteReusedBuffer(Blackhole bh) throws IOException {
        for (int i = 0; i < globBuilders.length; i++) {
            bh.consume(globWrite(globBuilders[i].apply(i), reusingAllocator));
        }
    }

    @Benchmark
    public void protobufBuildAndWrite(Blackhole bh) {
        for (int i = 0; i < protoBuilders.length; i++) {
            bh.consume(protoBuilders[i].apply(i).toByteArray());
        }
    }

    // ----

    private Glob globRead(int i) throws IOException {
        return family.readers[i].read(family.types[i], new SafeHeapReader(ByteBuffer.wrap(encoded[i])));
    }

    /** an exact byte[], like toByteArray(): the message can span several chunks, complete() returns its start */
    private byte[] globWrite(Glob glob) throws IOException {
        return globWrite(glob, BufferAllocator.create());
    }

    private final ReusingAllocator reusingAllocator = new ReusingAllocator();

    /** chunks are only borrowed for one write: globWrite copies the message out before the next one */
    static final class ReusingAllocator implements BufferAllocator {
        private final List<AllocatedBuffer> pool = new ArrayList<>();
        private int next;

        @Override
        public AllocatedBuffer allocateHeapBuffer(int capacity) {
            if (next < pool.size() && pool.get(next).array().length == capacity) {
                final AllocatedBuffer b = pool.get(next++);
                b.reset();
                return b;
            }
            final AllocatedBuffer b = new AllocatedBuffer(new byte[capacity], capacity);
            if (next < pool.size()) {
                pool.set(next++, b);
            } else {
                pool.add(b);
                next++;
            }
            return b;
        }

        ReusingAllocator rewind() {
            next = 0;
            return this;
        }
    }

    private byte[] globWrite(Glob glob, BufferAllocator allocator) throws IOException {
        if (allocator instanceof ReusingAllocator r) {
            r.rewind();
        }
        final BinaryWriter writer = BinaryWriter.newHeapInstance(allocator);
        family.writer.write(glob, writer);
        final byte[] out = new byte[writer.getTotalBytesWritten()];
        int pos = 0;
        for (AllocatedBuffer b = writer.complete(); b != null; b = b.getNext()) {
            final int len = b.limit() - b.position();
            System.arraycopy(b.array(), b.position(), out, pos, len);
            pos += len;
        }
        return out;
    }

    private static void collect(Message message, List<Message> into) {
        into.add(message);
        for (var e : message.getAllFields().entrySet()) {
            if (e.getKey().getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                if (e.getKey().isRepeated()) {
                    for (Object o : (List<?>) e.getValue()) {
                        collect((Message) o, into);
                    }
                } else {
                    collect((Message) e.getValue(), into);
                }
            }
        }
    }

    private static VarHandle memoizedSizeHandle() {
        for (Class<?> c = Shape0.class; c != null; c = c.getSuperclass()) {
            try {
                final Field f = c.getDeclaredField("memoizedSize");
                return MethodHandles.privateLookupIn(c, MethodHandles.lookup()).unreflectVarHandle(f);
            } catch (NoSuchFieldException e) {
                // look in the superclass
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("no memoizedSize in protobuf's message hierarchy");
    }

    static {
        System.setProperty("globsframework.field.no.check", "true");
    }
}
