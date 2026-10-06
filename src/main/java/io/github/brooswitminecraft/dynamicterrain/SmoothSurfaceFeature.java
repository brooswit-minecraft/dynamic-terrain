package io.github.brooswitminecraft.dynamicterrain;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Worldgen smoothing: runs once per newly generated chunk in the
 * top_layer_modification step and replaces 1-block steps on the surface with
 * layered blocks, so slopes are smoother. It only edits the chunk's own
 * columns. Its target comes from the generator's own natural heights
 * ({@link SurfaceSmoother}), so two neighbouring chunks agree along their edge.
 *
 * <p>It leaves a column alone unless the surface is plain natural ground: the top
 * block must have a layered form, the cell above it must be air (no trees, snow,
 * plants, water or structures), and the cell below must be solid. Roads, farms
 * and villages are therefore untouched. Gated by {@code worldgenSmoothing}.
 */
public class SmoothSurfaceFeature extends Feature<NoneFeatureConfiguration> {
    public SmoothSurfaceFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!DynamicTerrainConfig.WORLDGEN_SMOOTHING.get()) {
            return false;
        }
        WorldGenLevel level = context.level();
        int x0 = context.origin().getX();
        int z0 = context.origin().getZ();

        // Natural surface heights for the chunk plus a one-block border, all read from blocks. (Asking the
        // generator for heights costs milliseconds per column and made chunk generation several times slower.)
        // The border belongs to neighbouring chunks that may already have been smoothed, so for those the
        // fractional surface is rounded back to the nearest whole height; any error is at most a couple of
        // sixteenths at the seam.
        int[][] natural = new int[18][18];
        for (int dx = -1; dx <= 16; dx++) {
            for (int dz = -1; dz <= 16; dz++) {
                boolean own = dx >= 0 && dx <= 15 && dz >= 0 && dz <= 15;
                int x = x0 + dx;
                int z = z0 + dz;
                natural[dx + 1][dz + 1] = own || !level.hasChunk(x >> 4, z >> 4)
                        ? groundHeight(level, x, z, false)
                        : groundHeight(level, x, z, true);
            }
        }
        boolean changed = false;
        int[][] around = new int[3][3];
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                for (int a = 0; a < 3; a++) {
                    for (int b = 0; b < 3; b++) {
                        around[a][b] = natural[dx + a][dz + b];
                    }
                }
                changed |= smoothColumn(level, x0 + dx, z0 + dz, around);
            }
        }
        return changed;
    }

    /**
     * Top of the highest ground block in a column, looking down through trees, leaves and air. With
     * {@code roundLayered}, a floor-anchored layered block on top counts as its fractional height, rounded.
     */
    private static int groundHeight(LevelAccessor level, int x, int z, boolean roundLayered) {
        int minY = level.getMinBuildHeight();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1, z);
        while (pos.getY() > minY) {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.blocksMotion() && !state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS)) {
                break;
            }
            pos.move(0, -1, 0);
        }
        if (roundLayered && pos.getY() > minY) {
            BlockState ground = level.getBlockState(pos);
            if (ground.getBlock() instanceof LayeredBlock && !ground.getValue(LayeredBlock.ANCHOR).isCeiling()) {
                return (int) Math.round(pos.getY() + ground.getValue(LayeredBlock.LAYERS) / 16.0);
            }
        }
        return pos.getY() + 1;
    }

    private static boolean smoothColumn(LevelAccessor level, int x, int z, int[][] around) {
        int surface = around[1][1]; // top of the highest solid block
        BlockPos top = new BlockPos(x, surface - 1, z);
        BlockState topState = level.getBlockState(top);
        LayeredBlock layered = LayeredMaterials.layeredFor(topState.getBlock());
        if (layered == null || !level.getBlockState(top.above()).isAir() || !level.getBlockState(top.below()).canOcclude()) {
            return false; // not plain natural ground
        }
        SurfaceSmoother.Cell cell = SurfaceSmoother.cellOf(SurfaceSmoother.target(around));
        if (cell.y() == surface - 1 && cell.layers() == LayerMath.MAX_LAYERS) {
            return false; // the surface did not move
        }
        if (cell.y() == surface && cell.layers() == 0) {
            return false;
        }
        BlockState partial = layered.defaultBlockState()
                .setValue(LayeredBlock.LAYERS, Math.max(LayerMath.MIN_LAYERS, cell.layers()))
                .setValue(LayeredBlock.ANCHOR, Anchor.FLOOR);
        if (cell.y() >= surface) {
            // The surface rises into the air above: add a layered block there, and a covered grass top turns to dirt.
            if (cell.layers() == 0) {
                return false;
            }
            set(level, top.above(), partial);
            if (topState.is(Blocks.GRASS_BLOCK) || topState.is(Blocks.MYCELIUM) || topState.is(Blocks.PODZOL)) {
                set(level, top, Blocks.DIRT.defaultBlockState());
            }
            return true;
        }
        // The surface drops: the top block becomes a partial (or empty) cell. Blocks above it are air.
        if (cell.layers() == 0) {
            set(level, top, Blocks.AIR.defaultBlockState());
        } else {
            set(level, top, partial);
        }
        return true;
    }

    private static void set(LevelAccessor level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }
}
