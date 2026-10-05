package io.github.brooswitminecraft.dynamicterrain;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.world.level.block.Block;

/** Pairs each vanilla base block with its layered variant so smooth() can layer a full block. */
public final class LayeredMaterials {
    private static final Map<Block, Supplier<? extends LayeredBlock>> LAYERED_BY_BASE = new HashMap<>();

    private LayeredMaterials() {}

    public static void register(Block base, Supplier<? extends LayeredBlock> layered) {
        LAYERED_BY_BASE.put(base, layered);
    }

    /** The layered variant of a base block, or null if it has none. */
    public static LayeredBlock layeredFor(Block base) {
        Supplier<? extends LayeredBlock> layered = LAYERED_BY_BASE.get(base);
        return layered == null ? null : layered.get();
    }
}
