package io.github.brooswitminecraft.dynamicterrain;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Debug measurement of how stepped the loaded surface is, for judging worldgen smoothing. */
public final class SurfaceStats {
    private SurfaceStats() {}

    public static String measure(ServerLevel level, int cx, int cz, int radius) {
        int size = 2 * radius + 1;
        double[][] h = new double[size][size];
        int unloaded = 0;
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                int x = cx - radius + dx;
                int z = cz - radius + dz;
                if (!level.hasChunk(x >> 4, z >> 4)) {
                    h[dx][dz] = Double.NaN;
                    unloaded++;
                    continue;
                }
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                BlockState state = level.getBlockState(new BlockPos(x, y, z));
                if (state.getBlock() instanceof LayeredBlock && !state.getValue(LayeredBlock.ANCHOR).isCeiling()) {
                    h[dx][dz] = y + state.getValue(LayeredBlock.LAYERS) / 16.0;
                } else {
                    h[dx][dz] = y + 1;
                }
            }
        }
        double sum = 0;
        double max = 0;
        int count = 0;
        int wholeBlockSteps = 0;
        int layeredColumns = 0;
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                if (Double.isNaN(h[dx][dz])) {
                    continue;
                }
                if (h[dx][dz] != Math.floor(h[dx][dz])) {
                    layeredColumns++;
                }
                for (int[] n : new int[][] {{1, 0}, {0, 1}}) {
                    int nx = dx + n[0];
                    int nz = dz + n[1];
                    if (nx >= size || nz >= size || Double.isNaN(h[nx][nz])) {
                        continue;
                    }
                    double step = Math.abs(h[nx][nz] - h[dx][dz]);
                    sum += step;
                    max = Math.max(max, step);
                    count++;
                    if (step >= 0.99) {
                        wholeBlockSteps++;
                    }
                }
            }
        }
        return String.format("columns=%d unloaded=%d layeredColumns=%d pairs=%d meanStep=%.4f maxStep=%.3f wholeBlockSteps=%d (%.2f%%)",
                size * size - unloaded, unloaded, layeredColumns, count, count == 0 ? 0 : sum / count, max,
                wholeBlockSteps, count == 0 ? 0 : 100.0 * wholeBlockSteps / count);
    }
}
