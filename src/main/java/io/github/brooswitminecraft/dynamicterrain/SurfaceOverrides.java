package io.github.brooswitminecraft.dynamicterrain;

import java.util.Optional;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

/**
 * Declared surfaces that take priority over {@link SurfaceMath}'s automatic
 * inference. Blocks declare theirs in data (see {@link SurfaceOverrideLoader}),
 * either per-block or for every block sharing a {@link SurfaceMath.Family}; a
 * per-block declaration wins over a family one. {@link Surfaces} checks here first.
 * A reload swaps the table and clears {@link Surfaces}'s cache in the same step,
 * so an add, change or removal is visible on the next lookup of an already-queried
 * block, not just newly-queried ones.
 */
public final class SurfaceOverrides {
    private static volatile SurfaceOverrideTable table = new SurfaceOverrideTable();

    private SurfaceOverrides() {}

    static void setTable(SurfaceOverrideTable loaded) {
        table = loaded;
        Surfaces.invalidateCache();
    }

    /** Pure lookup: a per-block declaration wins over a family one; empty when neither is declared. */
    public static Optional<SurfaceProperties> resolve(String blockId, String family) {
        Optional<SurfaceProperties> declared = table.forBlock(blockId);
        return declared.isPresent() ? declared : table.forFamily(family);
    }

    public static Optional<SurfaceProperties> forBlock(Block block, SurfaceMath.Family family) {
        return resolve(BuiltInRegistries.BLOCK.getKey(block).toString(), family.name());
    }
}
