package io.github.brooswitminecraft.dynamicterrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Manual terrain shaping: right-click a block with a hoe to pull one
 * sixteenth of it toward you, via smooth(). Shovels are left alone so path
 * making stays vanilla.
 *
 * <p>A hoe also tills, so grading only takes over when smooth() would accept
 * the move, and then the click is cancelled on both client and server so
 * vanilla tilling cannot run. Grass is never gradable (it has no layered
 * form), so a hoe tills grass as usual. A full dirt block that could be tilled
 * (open air above) is graded only while sneaking, so farm plots stay easy.
 */
public final class GradingTool {
    private GradingTool() {}

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (event.getHand() != InteractionHand.MAIN_HAND || !stack.is(ItemTags.HOES)) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (wouldTill(level, pos, level.getBlockState(pos)) && !player.isShiftKeyDown()) {
            return;
        }
        Vec3 center = pos.getCenter();
        Direction toward = Direction.valueOf(Heading.toward(player.getX() - center.x, player.getZ() - center.z).name());
        if (!Smoothing.canSmooth(level, pos, toward)) {
            return;
        }
        if (!level.isClientSide()) {
            Smoothing.smooth(level, pos, toward);
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
    }

    /** Plain dirt with open air above is what a hoe would normally till. */
    private static boolean wouldTill(Level level, BlockPos pos, BlockState state) {
        return state.is(Blocks.DIRT) && level.getBlockState(pos.above()).isAir();
    }
}
