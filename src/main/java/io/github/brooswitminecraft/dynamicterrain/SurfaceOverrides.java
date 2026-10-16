package io.github.brooswitminecraft.dynamicterrain;

import java.util.Optional;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

/**
 * Declared surfaces that take priority over {@link SurfaceMath}'s automatic
 * inference. Blocks declare theirs in data (see {@link SurfaceOverrideLoader}),
 * either per-block or for every block sharing a {@link SurfaceMath.Family}; a
 * per-block declaration wins over a family one. {@link Surfaces} checks here first.
 */
public final class SurfaceOverrides {
    private static volatile SurfaceOverrideTable table = new SurfaceOverrideTable();

    private SurfaceOverrides() {}

    static void setTable(SurfaceOverrideTable loaded) {
        table = loaded;
    }

    public static Optional<SurfaceProperties> forBlock(Block block, SurfaceMath.Family family) {
        String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
        Optional<SurfaceProperties> declared = table.forBlock(blockId);
        return declared.isPresent() ? declared : table.forFamily(family.name());
    }
}
