package io.github.brooswitminecraft.dynamicterrain;

import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Manual terrain shaping: right-click a block with a shovel to pull one
 * sixteenth of it toward you, via smooth(). The move is rejected, and the
 * click left alone, when smooth() would reject it.
 */
public final class GradingTool {
    private GradingTool() {}

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (event.getHand() != InteractionHand.MAIN_HAND || !stack.is(ItemTags.SHOVELS) || player.level().isClientSide()) {
            return;
        }
        Vec3 center = event.getPos().getCenter();
        Direction toward = Direction.valueOf(Heading.toward(player.getX() - center.x, player.getZ() - center.z).name());
        if (Smoothing.smooth(player.level(), event.getPos(), toward)) {
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            event.setCanceled(true);
        }
    }
}
