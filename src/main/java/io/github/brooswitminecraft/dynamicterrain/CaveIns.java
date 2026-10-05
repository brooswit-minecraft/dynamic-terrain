package io.github.brooswitminecraft.dynamicterrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * V1 structural collapse. After a neighbouring block changes, any block in the
 * {@code dynamicterrain:supported} tag runs the bounded support search; if the
 * connected material cannot meet its required score, the block simply breaks
 * and drops its item. That update makes its own neighbours recheck, so cave-ins
 * cascade without falling entities and without any link to erosion. Gated by
 * {@code caveInsEnabled}.
 */
public final class CaveIns {
    public static final TagKey<Block> SUPPORTED = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(DynamicTerrainMod.MODID, "supported"));

    private static final int MAX_VISITED = 256;

    private CaveIns() {}

    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !DynamicTerrainConfig.CAVE_INS_ENABLED.get()) {
            return;
        }
        BlockPos changed = event.getPos();
        checkAndCollapse(level, changed);
        for (Direction direction : Direction.values()) {
            checkAndCollapse(level, changed.relative(direction));
        }
    }

    /** @return true if the block at {@code pos} was unsupported and has been broken. */
    public static boolean checkAndCollapse(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.is(SUPPORTED)) {
            return false;
        }
        int required = SupportMath.requiredScore(state.getDestroySpeed(level, pos), state.getBlock().getExplosionResistance());
        if (SupportSearch.isSupported((x, y, z) -> cellAt(level, x, y, z), pos.getX(), pos.getY(), pos.getZ(), required, MAX_VISITED)) {
            return false;
        }
        return level.destroyBlock(pos, true);
    }

    private static SupportSearch.Cell cellAt(LevelAccessor level, int x, int y, int z) {
        if (y < level.getMinBuildHeight()) {
            return SupportSearch.Cell.ANCHOR; // the bottom of the world holds everything
        }
        if (y >= level.getMaxBuildHeight()) {
            return SupportSearch.Cell.EMPTY;
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.hasChunkAt(pos)) {
            return SupportSearch.Cell.ANCHOR; // never collapse because a chunk is unloaded
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return SupportSearch.Cell.EMPTY;
        }
        if (state.getDestroySpeed(level, pos) < 0) {
            return SupportSearch.Cell.ANCHOR; // bedrock and other unbreakable blocks
        }
        return state.getCollisionShape(level, pos).isEmpty() ? SupportSearch.Cell.EMPTY : SupportSearch.Cell.SOLID;
    }
}
