package io.github.brooswitminecraft.dynamicterrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Heat as an erosion source: terrain touching lava is weathered slowly and
 * repeatedly, so lava regions round and degrade the geology beside them over
 * long periods. This is the V1 heat caller. It senses lava directly; a
 * Dynamic Atmosphere heat field can report through the same erode() later.
 * Gated by {@code erosionEnabled}.
 */
public final class HeatErosion {
    private static final int RADIUS = 16;
    private static final int HEIGHT = 8;

    private HeatErosion() {}

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 20 != 0 || !DynamicTerrainConfig.EROSION_ENABLED.get()) {
            return;
        }
        int samples = DynamicTerrainConfig.HEAT_SAMPLES_PER_SECOND.get();
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
            int sources = 0;
            for (Direction direction : Direction.values()) {
                if (level.getFluidState(pos.relative(direction)).is(FluidTags.LAVA)) {
                    sources++;
                }
            }
            double amount = HeatMath.amount(sources);
            if (amount > 0) {
                Erosion.Result result = Erosion.erode(level, pos, amount, random);
                if (result == Erosion.Result.MOVED || result == Erosion.Result.DEGRADED) {
                    changed++;
                }
            }
        }
        return changed;
    }
}
