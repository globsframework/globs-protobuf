package org.globsframework.grpc;

import org.globsframework.core.model.caller.FromGlobCallerService;
import org.globsframework.core.model.caller.ToGlobCallerService;

/**
 * Whether the generated callers of globs-generate are installed, i.e. the two properties core reads to answer
 * {@code FromGlobCallerFactory.generatedCallerFor} and {@code ToGlobCallerFactory.generated()}.
 * <p>
 * The same shape as {@link GlobFlavour}, and for the same reason : both services cache what the property named
 * when they were first asked, so a benchmark that wants the two arms in the same suite has to install the
 * property <em>and</em> reset the service around the moment the registries build their callers — which is
 * {@code PerfTypeFamily.create}, not the measured loop.
 * <p>
 * <b>The axis is not symmetrical, and reading it as "caller on / caller off" is wrong for the generated
 * flavours.</b> On the from-Glob side core has two sources : the type's own factory when it is a
 * {@code CallerGlobFactory} — which is what {@code -Dglobs.builder} installs — and only then the
 * {@link FromGlobCallerService} this switches. So for OBJECT and PRIMITIVE the writer already goes through a
 * caller with this {@link #OFF}, and {@link #ON} changes nothing there; it is DEFAULT, whose DefaultGlob no
 * generator built, that the service reaches. The to-Glob side has no such first source — nothing in the
 * emitted switch reads a Glob's layout — so the reader moves for every flavour.
 * <p>
 * In other words the pair worth quoting from this param is <b>DEFAULT × OFF/ON</b> on {@code write}, and any
 * flavour × OFF/ON on {@code read}.
 */
public enum CallerMode {
    OFF(null, null),
    ON("org.globsframework.model.generator.AsmCallerGeneratorService",
       "org.globsframework.model.generator.AsmCallerWriteGeneratorService");

    public final String fromGlobService;
    public final String toGlobService;

    CallerMode(String fromGlobService, String toGlobService) {
        this.fromGlobService = fromGlobService;
        this.toGlobService = toGlobService;
    }

    /** Runs {@code toRun} with this mode installed, and restores the previous setting afterwards. */
    public <T> T build(java.util.function.Supplier<T> toRun) {
        final String previousFromGlob = System.getProperty("globs.caller.fromGlob");
        final String previousToGlob = System.getProperty("globs.caller.toGlob");
        install(fromGlobService, toGlobService);
        try {
            return toRun.get();
        } finally {
            install(previousFromGlob, previousToGlob);
        }
    }

    private static void install(String fromGlobService, String toGlobService) {
        set("globs.caller.fromGlob", fromGlobService);
        set("globs.caller.toGlob", toGlobService);
        FromGlobCallerService.Builder.reset();
        ToGlobCallerService.Builder.reset();
    }

    private static void set(String property, String value) {
        if (value == null) {
            System.clearProperty(property);
        } else {
            System.setProperty(property, value);
        }
    }
}
