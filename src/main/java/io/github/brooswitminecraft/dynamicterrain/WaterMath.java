package io.github.brooswitminecraft.dynamicterrain;

/**
 * Pure water-erosion arithmetic. Moving (flowing) water erodes much faster
 * than still source water, scaled by how much water the flowing cell holds.
 * The numbers are a starting point to be tuned in play.
 */
public final class WaterMath {
    /** Erosion amount from a full-strength flowing cell. */
    public static final double MOVING = 0.5;
    /** Erosion amount from a still source block; much weaker. */
    public static final double STILL = 0.02;
    public static final int FULL_FLUID = 8;

    private WaterMath() {}

    /** @param fluidAmount 1..8, the fluid state's amount */
    public static double amount(boolean source, int fluidAmount) {
        if (source) {
            return STILL;
        }
        int clamped = Math.max(0, Math.min(FULL_FLUID, fluidAmount));
        return MOVING * clamped / FULL_FLUID;
    }
}
