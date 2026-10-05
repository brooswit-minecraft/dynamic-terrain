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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Manual terrain shaping: right-click a block with a pickaxe to pull one
 * sixteenth of it toward you, via smooth(). Pickaxes have no vanilla
 * right-click action on blocks, so nothing vanilla is displaced: shovels still
 * make paths and hoes still till.
 *
 * <p>Grading only takes over when smooth() would accept the move; otherwise
 * the click is left alone. When it applies, the click is cancelled on both
 * client and server so the client does not predict a vanilla action.
 */
public final class GradingTool {
    private GradingTool() {}

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (event.getHand() != InteractionHand.MAIN_HAND || !stack.is(ItemTags.PICKAXES)) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
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
}
