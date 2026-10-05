package io.github.brooswitminecraft.dynamicterrain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * erode(amount): a probabilistic policy over material movement and
 * degradation. Callers only report how much abuse a block took; this class
 * alone decides whether it changes.
 */
public final class Erosion {
    private Erosion() {}

    public enum Result { DISABLED, SKIPPED, RESISTED, MOVED, DEGRADED, STABLE }

    public static Result erode(Level level, BlockPos pos, double amount) {
        return erode(level, pos, amount, level.getRandom());
    }

    public static Result erode(Level level, BlockPos pos, double amount, RandomSource random) {
        if (level.isClientSide() || !DynamicTerrainConfig.EROSION_ENABLED.get()) {
            return Result.DISABLED;
        }
        BlockState state = level.getBlockState(pos);
        // 1. Does this block take part in erosion?
        if (!participates(state)) {
            return Result.SKIPPED;
        }
        // 2-3. Roll against resistance; failing the roll does nothing.
        double resistance = ErosionMath.resistance(state.getDestroySpeed(level, pos), state.getBlock().getExplosionResistance());
        if (random.nextDouble() >= ErosionMath.successChance(amount, resistance)) {
            return Result.RESISTED;
        }
        // 4. Move material downhill if a direction accepts it.
        int height = heightOf(state);
        List<Direction> directions = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
        Collections.shuffle(directions, new java.util.Random(random.nextLong()));
        for (Direction direction : directions) {
            if (ErosionMath.isDownhill(height, heightOf(level.getBlockState(pos.relative(direction))))
                    && Smoothing.smooth(level, pos, direction)) {
                return Result.MOVED;
            }
        }
        // 5. Material cannot move: degrade through the data-driven "damage" transition.
        return Transitions.applyTransition(level, pos, "damage") ? Result.DEGRADED : Result.STABLE;
    }

    private static boolean participates(BlockState state) {
        return state.getBlock() instanceof LayeredBlock || LayeredMaterials.layeredFor(state.getBlock()) != null;
    }

    /** Height in layers: air 0, layered blocks their layer count, anything else a full block. */
    static int heightOf(BlockState state) {
        if (state.isAir()) {
            return 0;
        }
        return state.getBlock() instanceof LayeredBlock ? state.getValue(LayeredBlock.LAYERS) : LayerMath.MAX_LAYERS;
    }
}
