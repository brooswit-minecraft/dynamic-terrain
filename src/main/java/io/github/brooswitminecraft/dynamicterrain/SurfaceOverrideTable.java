package io.github.brooswitminecraft.dynamicterrain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Pure lookup behind the surface override registry: a block id or a
 * {@link SurfaceMath.Family} name, each mapping to a declared
 * {@link SurfaceProperties}. Checked before automatic inference by
 * {@link Surfaces}; unknown entries fall through to it untouched.
 */
public final class SurfaceOverrideTable {
    private final Map<String, SurfaceProperties> blocks = new HashMap<>();
    private final Map<String, SurfaceProperties> families = new HashMap<>();

    /** Declare {@code blockId}'s surface, taking priority over any family override. A later put wins. */
    public void putBlock(String blockId, SurfaceProperties properties) {
        blocks.put(blockId, properties);
    }

    /** Declare every block of {@code family} (named after {@link SurfaceMath.Family}) a surface, unless overridden per-block. */
    public void putFamily(String family, SurfaceProperties properties) {
        families.put(family.toLowerCase(java.util.Locale.ROOT), properties);
    }

    public Optional<SurfaceProperties> forBlock(String blockId) {
        return Optional.ofNullable(blocks.get(blockId));
    }

    public Optional<SurfaceProperties> forFamily(String family) {
        return Optional.ofNullable(families.get(family.toLowerCase(java.util.Locale.ROOT)));
    }

    public int size() {
        return blocks.size() + families.size();
    }
}
