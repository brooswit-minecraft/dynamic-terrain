package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class SurfaceCacheTest {
    private static final SurfaceProperties FIRST = new SurfaceProperties(0.1, 0.1, 0.1, 0.1);
    private static final SurfaceProperties SECOND = new SurfaceProperties(0.9, 0.9, 0.9, 0.9);

    @Test
    void repeatedLookupIsMemoized() {
        SurfaceCache<String> cache = new SurfaceCache<>();
        AtomicInteger calls = new AtomicInteger();
        SurfaceProperties first = cache.computeIfAbsent("a", k -> {
            calls.incrementAndGet();
            return FIRST;
        });
        SurfaceProperties second = cache.computeIfAbsent("a", k -> {
            calls.incrementAndGet();
            return SECOND;
        });
        assertSame(first, second, "second lookup must return the memoized value, not recompute");
        assertEquals(1, calls.get());
    }

    @Test
    void clearForcesRecomputeOnNextLookup() {
        SurfaceCache<String> cache = new SurfaceCache<>();
        cache.computeIfAbsent("a", k -> FIRST);
        cache.clear();
        SurfaceProperties after = cache.computeIfAbsent("a", k -> SECOND);
        assertEquals(SECOND, after, "a cleared cache must recompute: this is what a reload relies on");
    }

    @Test
    void differentKeysAreIndependent() {
        SurfaceCache<String> cache = new SurfaceCache<>();
        cache.computeIfAbsent("a", k -> FIRST);
        cache.computeIfAbsent("b", k -> SECOND);
        assertEquals(FIRST, cache.computeIfAbsent("a", k -> SECOND));
        assertEquals(SECOND, cache.computeIfAbsent("b", k -> FIRST));
    }
}
