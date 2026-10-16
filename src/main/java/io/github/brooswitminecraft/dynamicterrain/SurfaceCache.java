package io.github.brooswitminecraft.dynamicterrain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Memoizes a {@link SurfaceProperties} computation per key. {@link #clear()}
 * drops every memoized value, so a key computed before a {@link SurfaceOverrides}
 * reload is recomputed (and can pick up a newly added, changed or removed
 * override) on its next lookup rather than keeping a stale result forever.
 */
public final class SurfaceCache<K> {
    private final Map<K, SurfaceProperties> cache = new ConcurrentHashMap<>();

    public SurfaceProperties computeIfAbsent(K key, Function<K, SurfaceProperties> compute) {
        return cache.computeIfAbsent(key, compute);
    }

    public void clear() {
        cache.clear();
    }
}
