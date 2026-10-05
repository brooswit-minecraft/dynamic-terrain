package io.github.brooswitminecraft.dynamicterrain;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * smooth(direction): move exactly one sixteenth of material from a block into
 * its neighbour. Deterministic; erosion and manual tools call this.
 */
public final class Smoothing {
    private Smoothing() {}

    /** A planned move: nothing has changed in the world yet. */
    private record Move(LayeredBlock block, LayeredBlock destinationBlock, BlockPos source, BlockPos destination, SmoothRules.Plan plan) {}

    /** @return true if a layer moved, false if the move was rejected. */
    public static boolean smooth(Level level, BlockPos pos, Direction direction) {
        if (level.isClientSide()) {
            return false;
        }
        Optional<Move> move = plan(level, pos, direction);
        if (move.isEmpty()) {
            return false;
        }
        Move m = move.get();
        level.setBlock(m.source(), stateOf(m.block(), m.plan().source()), Block.UPDATE_ALL);
        level.setBlock(m.destination(), stateOf(m.destinationBlock(), m.plan().destination()), Block.UPDATE_ALL);
        return true;
    }

    /** Whether smooth() would accept this move. Changes nothing, and works on either side of the connection. */
    public static boolean canSmooth(Level level, BlockPos pos, Direction direction) {
        return plan(level, pos, direction).isPresent();
    }

    private static Optional<Move> plan(Level level, BlockPos pos, Direction direction) {
        BlockState sourceState = level.getBlockState(pos);
        LayeredBlock layeredBlock = sourceState.getBlock() instanceof LayeredBlock lb ? lb
                : LayeredMaterials.layeredFor(sourceState.getBlock());
        if (layeredBlock == null) {
            return Optional.empty();
        }
        SmoothRules.Cell source = sourceState.getBlock() instanceof LayeredBlock
                ? new SmoothRules.Cell(sourceState.getValue(LayeredBlock.LAYERS), sourceState.getValue(LayeredBlock.ANCHOR).isCeiling())
                : new SmoothRules.Cell(LayerMath.MAX_LAYERS, false);

        BlockPos destPos = pos.relative(direction);
        BlockState destState = level.getBlockState(destPos);
        SmoothRules.Destination kind;
        SmoothRules.Cell dest = null;
        LayeredBlock destBlock = layeredBlock;
        if (destState.isAir()) {
            kind = SmoothRules.Destination.EMPTY;
        } else if (destState.getBlock() instanceof LayeredBlock destLayered && LayeredMaterials.sameGroup(layeredBlock, destLayered)) {
            destBlock = destLayered;
            kind = SmoothRules.Destination.SAME_MATERIAL;
            dest = new SmoothRules.Cell(destState.getValue(LayeredBlock.LAYERS), destState.getValue(LayeredBlock.ANCHOR).isCeiling());
        } else {
            kind = SmoothRules.Destination.BLOCKED;
        }
        LayeredBlock finalDestBlock = destBlock;
        return SmoothRules.plan(source, kind, dest, anchorSupported(level, pos, destPos, source.ceiling()))
                .map(plan -> new Move(layeredBlock, finalDestBlock, pos, destPos, plan));
    }

    /**
     * Whether a new layer at {@code destPos} would stay anchored. The source
     * cell never counts as support: it has just lost a layer, so it is no
     * longer a full face.
     */
    private static boolean anchorSupported(Level level, BlockPos sourcePos, BlockPos destPos, boolean ceiling) {
        Direction toSupport = ceiling ? Direction.UP : Direction.DOWN;
        BlockPos supportPos = destPos.relative(toSupport);
        if (supportPos.equals(sourcePos)) {
            return false;
        }
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, toSupport.getOpposite());
    }

    private static BlockState stateOf(LayeredBlock block, SmoothRules.Cell cell) {
        if (cell == null) {
            return Blocks.AIR.defaultBlockState();
        }
        return block.defaultBlockState()
                .setValue(LayeredBlock.LAYERS, cell.layers())
                .setValue(LayeredBlock.ANCHOR, cell.ceiling() ? Anchor.CEILING : Anchor.FLOOR);
    }
}
