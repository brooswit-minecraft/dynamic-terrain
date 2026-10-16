package io.github.brooswitminecraft.dynamicterrain;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Vehicle-facing surface lookup. Vehicles ask the contacted block for driving
 * properties; they never modify terrain directly.
 */
public final class Surfaces {
    private static final Map<Block, SurfaceProperties> CACHE = new ConcurrentHashMap<>();

    private Surfaces() {}

    public static SurfaceProperties at(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return SurfaceProperties.PAVEMENT;
        }
        return CACHE.computeIfAbsent(state.getBlock(), block -> infer(block, state, level, pos));
    }

    /**
     * Per-texel evidence for the tire contact patch on one face of the block at {@code pos}, from its
     * canonical texture (never the player's resource pack), integrated over the texels crossed between
     * {@code from} and {@code to} this physics step. Empty when the block's canonical texture can't be
     * resolved (e.g. a blockstate this doesn't understand, or assets absent on a bare dedicated server):
     * callers fall back to {@link #at} alone in that case.
     */
    public static Optional<TireContactSampler.ContactPatch> contactPatch(BlockGetter level, BlockPos pos, Direction face,
            double fromX, double fromY, double fromZ, double toX, double toY, double toZ) {
        BlockState state = level.getBlockState(pos);
        Optional<ResourceLocation> textureId = BlockFaceTextures.textureFor(state, face);
        if (textureId.isEmpty()) {
            return Optional.empty();
        }
        return CanonicalTextures.at(textureId.get())
                .map(field -> TireContactSampler.integrate(field, toFace(face), fromX, fromY, fromZ, toX, toY, toZ));
    }

    private static Face toFace(Direction direction) {
        return switch (direction) {
            case UP -> Face.UP;
            case DOWN -> Face.DOWN;
            case NORTH -> Face.NORTH;
            case SOUTH -> Face.SOUTH;
            case EAST -> Face.EAST;
            case WEST -> Face.WEST;
        };
    }

    private static SurfaceProperties infer(Block block, BlockState state, BlockGetter level, BlockPos pos) {
        double hardness = state.getDestroySpeed(level, pos);
        return SurfaceMath.infer(familyOf(state.getSoundType()), block.getFriction(), hardness, block.getExplosionResistance());
    }

    static SurfaceMath.Family familyOf(SoundType sound) {
        if (sound == SoundType.STONE || sound == SoundType.DEEPSLATE || sound == SoundType.DEEPSLATE_BRICKS
                || sound == SoundType.DEEPSLATE_TILES || sound == SoundType.POLISHED_DEEPSLATE || sound == SoundType.BASALT
                || sound == SoundType.CALCITE || sound == SoundType.TUFF || sound == SoundType.NETHER_BRICKS
                || sound == SoundType.COPPER || sound == SoundType.AMETHYST) {
            return SurfaceMath.Family.STONE;
        }
        if (sound == SoundType.METAL || sound == SoundType.ANVIL || sound == SoundType.CHAIN || sound == SoundType.NETHERITE_BLOCK) {
            return SurfaceMath.Family.METAL;
        }
        if (sound == SoundType.WOOD || sound == SoundType.BAMBOO_WOOD || sound == SoundType.CHERRY_WOOD
                || sound == SoundType.NETHER_WOOD) {
            return SurfaceMath.Family.WOOD;
        }
        if (sound == SoundType.GLASS) {
            return SurfaceMath.Family.GLASS;
        }
        if (sound == SoundType.GRAVEL) {
            return SurfaceMath.Family.GRAVEL;
        }
        if (sound == SoundType.SAND || sound == SoundType.SOUL_SAND || sound == SoundType.SOUL_SOIL) {
            return SurfaceMath.Family.SAND;
        }
        if (sound == SoundType.GRASS || sound == SoundType.MOSS || sound == SoundType.ROOTED_DIRT || sound == SoundType.NYLIUM
                || sound == SoundType.WART_BLOCK || sound == SoundType.MUD || sound == SoundType.PACKED_MUD) {
            return SurfaceMath.Family.GRASS;
        }
        if (sound == SoundType.WOOL) {
            return SurfaceMath.Family.WOOL;
        }
        if (sound == SoundType.SNOW || sound == SoundType.POWDER_SNOW) {
            return SurfaceMath.Family.SNOW;
        }
        if (sound == SoundType.SLIME_BLOCK) {
            return SurfaceMath.Family.SLIME;
        }
        if (sound == SoundType.HONEY_BLOCK) {
            return SurfaceMath.Family.HONEY;
        }
        return SurfaceMath.Family.OTHER;
    }
}
