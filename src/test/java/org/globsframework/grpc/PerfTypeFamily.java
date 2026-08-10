package org.globsframework.grpc;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.metamodel.GlobTypeBuilder;
import org.globsframework.core.metamodel.GlobTypeBuilderFactory;
import org.globsframework.core.metamodel.fields.*;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.grpc.reader.ProtobufReader;
import org.globsframework.grpc.reader.SafeHeapReader;
import org.globsframework.grpc.writer.AllocatedBuffer;
import org.globsframework.grpc.writer.BinaryWriter;
import org.globsframework.grpc.writer.BufferAllocator;
import org.globsframework.grpc.writer.ProtobufWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A family of several *different* GlobTypes, all built with the same {@link GlobFlavour}, with the protobuf
 * writer/reader and the sample data that go with them.
 * <p>
 * Several shapes on purpose : with a single type the call sites in the serializer loops are monomorphic and the
 * JIT inlines the one accessor implementation it ever sees, which flatters whichever flavour is measured — and
 * flatters the generated one most, since it is exactly the case where one generated class per type costs
 * nothing. Four shapes of different sizes (which also spans the 32 / 64 field specializations of both core and
 * the generators) keep the accessor call sites polymorphic, as they would be in a real application.
 */
public class PerfTypeFamily {
    /** field counts of the four shapes; all &lt;= 64, above which the primitive generator falls back to DefaultGlob */
    private static final int[] ALL_SHAPES = {6, 15, 28, 45};

    /**
     * {@code -Dperf.shapes=1} keeps only the first shape : that is the control experiment for the polymorphism
     * of the accessor call sites — one type makes them monomorphic again, which is the case a benchmark should
     * not be quoted from, but the case that says how much of the difference megamorphism accounts for.
     */
    private static final int[] SHAPES =
            Arrays.copyOf(ALL_SHAPES, Math.min(Integer.getInteger("perf.shapes", ALL_SHAPES.length), ALL_SHAPES.length));

    public final GlobFlavour flavour;
    public final GlobType[] types;
    public final Glob[] data;
    public final byte[][] encoded;
    public final ProtobufWriter writer;
    public final ProtobufReader.GlobReader[] readers;

    private PerfTypeFamily(GlobFlavour flavour, GlobType[] types) {
        this.flavour = flavour;
        this.types = types;
        this.data = new Glob[types.length];
        for (int i = 0; i < types.length; i++) {
            data[i] = fill(types[i].instantiate(), i, true);
        }
        this.writer = ProtobufWriter.Builder.init().add(Arrays.asList(types)).build();
        final ProtobufReader reader = ProtobufReader.Builder.init(GlobType::instantiate)
                .add(Arrays.asList(types))
                .build();
        this.readers = new ProtobufReader.GlobReader[types.length];
        this.encoded = new byte[types.length][];
        for (int i = 0; i < types.length; i++) {
            readers[i] = reader.getReader(types[i]);
            encoded[i] = write(data[i]);
        }
    }

    public static PerfTypeFamily create(GlobFlavour flavour) {
        return flavour.build(() -> {
            GlobType[] types = new GlobType[SHAPES.length];
            for (int i = 0; i < SHAPES.length; i++) {
                types[i] = declare(flavour.name() + "Shape" + i, SHAPES[i]);
            }
            return new PerfTypeFamily(flavour, types);
        });
    }

    public byte[] write(Glob glob) {
        return write(writer, glob);
    }

    private static byte[] write(ProtobufWriter writer, Glob glob) {
        try {
            final BinaryWriter binaryWriter = BinaryWriter.newHeapInstance(BufferAllocator.create());
            writer.write(glob, binaryWriter);
            return toBytes(binaryWriter.complete());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Glob read(int shape) {
        try {
            return readers[shape].read(types[shape], new SafeHeapReader(ByteBuffer.wrap(encoded[shape])));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** the message can span several chunks, and {@code complete()} returns the last allocated one (its start) */
    public static byte[] toBytes(AllocatedBuffer buffer) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (AllocatedBuffer current = buffer; current != null; current = current.getNext()) {
            out.write(current.array(), current.position(), current.limit() - current.position());
        }
        return out.toByteArray();
    }

    /**
     * Cycles through the field kinds the protobuf mapping supports, then closes with a self reference so nested
     * messages — and the Glob instantiation the reader does for them — are part of the measure.
     */
    private static GlobType declare(String name, int fieldCount) {
        final GlobTypeBuilder builder = GlobTypeBuilderFactory.create(name);
        final GlobType[] self = new GlobType[1];
        for (int i = 0; i < fieldCount - 2; i++) {
            final int number = i + 1;
            switch (i % 10) {
                case 0 -> builder.declareStringField("str" + i, ProtobufField.create(number));
                case 1 -> builder.declareIntegerField("int" + i, ProtobufField.create(number, ProtobufField.GrpcType.int32));
                case 2 -> builder.declareLongField("lng" + i, ProtobufField.create(number, ProtobufField.GrpcType.int64));
                case 3 -> builder.declareDoubleField("dbl" + i, ProtobufField.create(number));
                case 4 -> builder.declareBooleanField("bool" + i, ProtobufField.create(number));
                case 5 -> builder.declareIntegerField("sint" + i, ProtobufField.create(number, ProtobufField.GrpcType.sint32));
                case 6 -> builder.declareIntegerArrayField("intArr" + i, ProtobufField.create(number, ProtobufField.GrpcType.int32));
                case 7 -> builder.declareLongArrayField("lngArr" + i, ProtobufField.create(number, ProtobufField.GrpcType.int64));
                case 8 -> builder.declareDoubleArrayField("dblArr" + i, ProtobufField.create(number));
                default -> builder.declareStringArrayField("strArr" + i, ProtobufField.create(number));
            }
        }
        builder.declareGlobField("child", () -> self[0], ProtobufField.create(fieldCount - 1));
        builder.declareGlobArrayField("children", () -> self[0], ProtobufField.create(fieldCount));
        self[0] = builder.build();
        return self[0];
    }

    /** deterministic values, identical for every flavour so the encoded bytes are comparable */
    public static MutableGlob fill(MutableGlob glob, int seed, boolean withChildren) {
        for (Field field : glob.getType().getFields()) {
            final int i = field.getIndex() + seed;
            if (field instanceof StringField f) {
                glob.set(f, "value" + i);
            } else if (field instanceof IntegerField f) {
                glob.set(f, i * 7);
            } else if (field instanceof LongField f) {
                glob.set(f, i * 1_000_003L);
            } else if (field instanceof DoubleField f) {
                glob.set(f, i + 0.5);
            } else if (field instanceof BooleanField f) {
                glob.set(f, i % 2 == 0);
            } else if (field instanceof IntegerArrayField f) {
                glob.set(f, new int[]{i, i + 1, i + 2});
            } else if (field instanceof LongArrayField f) {
                glob.set(f, new long[]{i, i + 1, i + 2});
            } else if (field instanceof DoubleArrayField f) {
                glob.set(f, new double[]{i + 0.5, i + 1.5});
            } else if (field instanceof StringArrayField f) {
                glob.set(f, new String[]{"a" + i, "b" + i});
            } else if (withChildren && field instanceof GlobField<?> f) {
                glob.set(f, fill(f.getTargetType().instantiate(), seed + 1, false));
            } else if (withChildren && field instanceof GlobArrayField<?> f) {
                glob.set(f, new Glob[]{fill(f.getTargetType().instantiate(), seed + 2, false),
                        fill(f.getTargetType().instantiate(), seed + 3, false)});
            }
        }
        return glob;
    }

    /** the values, in declaration order, as text — the cheapest way to compare two Globs of two distinct types */
    public static List<String> describe(Glob glob) {
        final List<String> values = new ArrayList<>();
        for (Field field : glob.getType().getFields()) {
            final Object value = glob.getValue(field);
            if (value instanceof Glob child) {
                values.add(field.getName() + "=" + describe(child));
            } else if (value instanceof Glob[] children) {
                values.add(field.getName() + "=" + Arrays.stream(children).map(PerfTypeFamily::describe).toList());
            } else if (value != null && value.getClass().isArray()) {
                values.add(field.getName() + "=" + (value instanceof Object[] o ? Arrays.deepToString(o)
                        : value instanceof int[] a ? Arrays.toString(a)
                        : value instanceof long[] a ? Arrays.toString(a)
                        : value instanceof double[] a ? Arrays.toString(a)
                        : String.valueOf(value)));
            } else {
                values.add(field.getName() + "=" + value);
            }
        }
        return values;
    }
}
