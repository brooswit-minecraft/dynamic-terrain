package io.github.brooswitminecraft.dynamicterrain;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Walking entities as an erosion source. Every half second, living entities
 * near a player that are on the ground and moving report mass x speed to
 * erode() for the block under their feet. Gated by {@code erosionEnabled}.
 */
public final class EntityErosion {
    private static final double RADIUS = 24.0;

    private EntityErosion() {}

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 10 != 0 || !DynamicTerrainConfig.EROSION_ENABLED.get()
                || !DynamicTerrainConfig.ENTITY_EROSION.get()) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            AABB area = player.getBoundingBox().inflate(RADIUS);
            for (LivingEntity entity : player.serverLevel().getEntitiesOfClass(LivingEntity.class, area)) {
                if (!entity.onGround() || entity.isSpectator()) {
                    continue;
                }
                double speed = Math.hypot(entity.getX() - entity.xo, entity.getZ() - entity.zo) * 20.0;
                double amount = EntityMath.amount(EntityMath.massKg(entity.getBbWidth(), entity.getBbHeight()), speed);
                if (amount > 0) {
                    BlockPos under = entity.getOnPos();
                    Erosion.erode(entity.level(), under, amount);
                }
            }
        }
    }
}
