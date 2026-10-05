package io.github.brooswitminecraft.dynamicterrain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Pure lookup behind the transition registry: transition name -> block id ->
 * result block id. Unknown transitions and blocks have no result, which makes
 * applying them a harmless no-op.
 */
public final class TransitionTable {
    private final Map<String, Map<String, String>> results = new HashMap<>();

    /** Declare that {@code from} becomes {@code to} under {@code transition}. A later put wins. */
    public void put(String transition, String from, String to) {
        results.computeIfAbsent(transition, k -> new HashMap<>()).put(from, to);
    }

    public Optional<String> resultFor(String transition, String from) {
        return Optional.ofNullable(results.get(transition)).map(m -> m.get(from));
    }

    public int size() {
        return results.values().stream().mapToInt(Map::size).sum();
    }
}
