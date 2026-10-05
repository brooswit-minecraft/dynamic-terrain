package io.github.brooswitminecraft.dynamicterrain;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Public input for vehicles. A slipping tire does not know whether it is on
 * sand or obsidian: it reports how hard it is sliding and erosion alone
 * decides whether the surface changes. Call once per physics tick per wheel
 * with the block that supports the contact patch.
 */
public final class TireSlip {
    private TireSlip() {}

    /**
     * @param level the world; ignored on the client
     * @param contactBlock the block under the contact patch
     * @param slipSpeed contact-patch sliding speed in m/s; at or below 0 reports nothing
     * @param wheelLoadKg mass carried by this wheel
     * @return what erosion did (DISABLED when erosion is switched off)
     */
    public static Erosion.Result report(Level level, BlockPos contactBlock, double slipSpeed, double wheelLoadKg) {
        double amount = SlipMath.amount(slipSpeed, wheelLoadKg);
        if (amount <= 0) {
            return Erosion.Result.SKIPPED;
        }
        return Erosion.erode(level, contactBlock, amount);
    }
}
