package io.github.brooswitminecraft.dynamicterrain;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Generic environmental transitions. Systems call
 * {@code applyTransition(level, pos, "moisture")}; blocks declare what they
 * become in data (see {@link TransitionLoader}). Emitters never learn about
 * individual blocks, and unknown transitions or blocks are no-ops.
 */
public final class Transitions {
    private static volatile TransitionTable table = new TransitionTable();

    private Transitions() {}

    static void setTable(TransitionTable loaded) {
        table = loaded;
    }

    /** @return true if the block at {@code pos} changed. */
    public static boolean applyTransition(Level level, BlockPos pos, String transition) {
        if (level.isClientSide()) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        String from = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        Optional<Block> result = table.resultFor(transition, from)
                .map(ResourceLocation::tryParse)
                .flatMap(id -> id == null ? Optional.empty() : BuiltInRegistries.BLOCK.getOptional(id));
        if (result.isEmpty()) {
            return false;
        }
        BlockState next = result.get().defaultBlockState();
        for (var property : state.getProperties()) {
            if (next.hasProperty(property)) {
                next = copy(state, next, property);
            }
        }
        return level.setBlock(pos, next, Block.UPDATE_ALL);
    }

    private static <T extends Comparable<T>> BlockState copy(BlockState from, BlockState to,
            net.minecraft.world.level.block.state.properties.Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }
}
