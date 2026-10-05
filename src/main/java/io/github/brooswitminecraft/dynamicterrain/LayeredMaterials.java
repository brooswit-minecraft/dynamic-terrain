package io.github.brooswitminecraft.dynamicterrain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.world.level.block.Block;

/** Pairs each vanilla base block with its layered variant so smooth() can layer a full block. */
public final class LayeredMaterials {
    private static final Map<Block, Supplier<? extends LayeredBlock>> LAYERED_BY_BASE = new HashMap<>();

    private static final Map<String, List<Supplier<? extends LayeredBlock>>> GROUPS = new HashMap<>();

    private LayeredMaterials() {}

    /**
     * Layered blocks of one group stack on each other when material moves between them (grass and dirt
     * are both "soil"). A moved layer landing on an existing layered block adopts that block's type.
     */
    public static void joinGroup(String group, Supplier<? extends LayeredBlock> layered) {
        GROUPS.computeIfAbsent(group, g -> new ArrayList<>()).add(layered);
    }

    public static boolean sameGroup(LayeredBlock a, LayeredBlock b) {
        if (a == b) {
            return true;
        }
        for (List<Supplier<? extends LayeredBlock>> members : GROUPS.values()) {
            boolean hasA = false;
            boolean hasB = false;
            for (Supplier<? extends LayeredBlock> member : members) {
                LayeredBlock block = member.get();
                hasA |= block == a;
                hasB |= block == b;
            }
            if (hasA && hasB) {
                return true;
            }
        }
        return false;
    }

    public static void register(Block base, Supplier<? extends LayeredBlock> layered) {
        LAYERED_BY_BASE.put(base, layered);
    }

    /** The layered variant of a base block, or null if it has none. */
    public static LayeredBlock layeredFor(Block base) {
        Supplier<? extends LayeredBlock> layered = LAYERED_BY_BASE.get(base);
        return layered == null ? null : layered.get();
    }
}
