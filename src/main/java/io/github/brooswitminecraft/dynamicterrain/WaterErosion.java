package io.github.brooswitminecraft.dynamicterrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Water as an erosion source. Each second, a bounded number of random
 * positions around each player are sampled; an erodible block next to water
 * reports the abuse to erode(), which alone decides whether it changes.
 * Gated by {@code erosionEnabled}.
 */
public final class WaterErosion {
    private static final int RADIUS = 16;
    private static final int HEIGHT = 6;

    private WaterErosion() {}

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 20 != 0 || !DynamicTerrainConfig.EROSION_ENABLED.get()) {
            return;
        }
        int samples = DynamicTerrainConfig.WATER_SAMPLES_PER_SECOND.get();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sampleAround(player.serverLevel(), player.blockPosition(), samples, player.getRandom());
        }
    }

    /** Sample random positions near {@code center}. Returns how many blocks eroded. */
    public static int sampleAround(ServerLevel level, BlockPos center, int samples, RandomSource random) {
        int changed = 0;
        for (int i = 0; i < samples; i++) {
            BlockPos pos = center.offset(random.nextInt(2 * RADIUS + 1) - RADIUS,
                    random.nextInt(2 * HEIGHT + 1) - HEIGHT, random.nextInt(2 * RADIUS + 1) - RADIUS);
            if (!level.isLoaded(pos) || !Erosion.takesPart(level, pos, level.getBlockState(pos))) {
                continue;
            }
            double amount = strongestWaterAround(level, pos);
            if (amount > 0) {
                Erosion.Result result = Erosion.erode(level, pos, amount, random);
                if (result == Erosion.Result.MOVED || result == Erosion.Result.DEGRADED) {
                    changed++;
                }
            }
        }
        return changed;
    }

    /** The strongest water contact among the sides and the cell above, or 0. */
    static double strongestWaterAround(ServerLevel level, BlockPos pos) {
        double strongest = 0;
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN) {
                continue;
            }
            FluidState fluid = level.getFluidState(pos.relative(direction));
            if (!fluid.isEmpty() && fluid.is(FluidTags.WATER)) {
                strongest = Math.max(strongest, WaterMath.amount(fluid.isSource(), fluid.getAmount()));
            }
        }
        return strongest;
    }
}
