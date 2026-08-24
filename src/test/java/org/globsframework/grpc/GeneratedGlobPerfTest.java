package org.globsframework.grpc;

import org.globsframework.core.model.Glob;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Protobuf serialization of Globs, core's DefaultGlob against the two ASM flavours of globs-generate, each
 * with and without the generated callers.
 * <p>
 * What is being compared is the accessor path : the leaf serializers/deserializers resolve a typed accessor once
 * ({@code getGetAccessor}/{@code getSetAccessor}) and then only call it, so the whole difference between the
 * flavours is how a value is read from / written into the Glob — a lookup in DefaultGlob's Object[] (plus
 * boxing for the primitives) against a generated field access.
 * <p>
 * Each measurement walks {@link PerfTypeFamily}'s four distinct types rather than one, so the accessor call
 * sites stay polymorphic; a one-type benchmark would let the JIT inline a single implementation and would not
 * say much about a real application.
 * <p>
 * The second axis is {@link CallerMode}, the generated callers. It is what makes the per-field call site
 * monomorphic — a generated class holding each leaf serializer in a static final, instead of an array walked
 * by a loop that sees every leaf class in the process. JMH forks a JVM per param combination, which is what
 * this needs : both services cache the property they were loaded with, and one arm must not warm up the
 * other's profile. Read the axis with {@link CallerMode}'s asymmetry in mind — OFF already means "callered"
 * for the writer of a generated flavour, so the from-Glob caller's own cost shows on the DEFAULT rows.
 * <p>
 * Run it with {@code mvn test-compile exec:java} on the main below, or from the IDE.
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 3, time = 3)
@Fork(2)
@State(Scope.Thread)
public class GeneratedGlobPerfTest {

    @Param({"DEFAULT", "OBJECT", "PRIMITIVE"})
    public String flavour;

    @Param({"OFF", "ON"})
    public String caller;

    private PerfTypeFamily family;

    @Setup
    public void setup() {
        // the registries resolve their callers while the family is built, so the mode has to be installed
        // around create() and nowhere else
        family = CallerMode.valueOf(caller).build(() -> PerfTypeFamily.create(GlobFlavour.valueOf(flavour)));
    }

    /** serialization only : the Globs are built once, the measure is the walk over the accessors */
    @Benchmark
    public void write(Blackhole blackhole) {
        for (int i = 0; i < family.data.length; i++) {
            blackhole.consume(family.write(family.data[i]));
        }
    }

    /** deserialization : instantiates the Globs (including the nested ones) and sets every field */
    @Benchmark
    public void read(Blackhole blackhole) {
        for (int i = 0; i < family.types.length; i++) {
            blackhole.consume(family.read(i));
        }
    }

    /** allocation + set of every field, without any protobuf, to separate the Glob cost from the wire cost */
    @Benchmark
    public void instantiateAndFill(Blackhole blackhole) {
        for (int i = 0; i < family.types.length; i++) {
            blackhole.consume(PerfTypeFamily.fill(family.types[i].instantiate(), i, false));
        }
    }

    /** reading every field back, the other half of the accessor path */
    @Benchmark
    public void readAllFields(Blackhole blackhole) {
        for (Glob glob : family.data) {
            for (int f = 0; f < glob.getType().getFieldCount(); f++) {
                blackhole.consume(glob.getValue(glob.getType().getField(f)));
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder()
                .include(GeneratedGlobPerfTest.class.getSimpleName())
                .build())
                .run();
    }

    static {
        System.setProperty("globsframework.field.no.check", "true");
    }
}
