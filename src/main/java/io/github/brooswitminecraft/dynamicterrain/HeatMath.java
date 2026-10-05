package io.github.brooswitminecraft.dynamicterrain;

/**
 * Pure heat-erosion arithmetic. Heat weathers geology slowly and repeatedly,
 * so the amount is small and scales with how many neighbouring cells are hot.
 */
public final class HeatMath {
    /** Erosion amount per sample for a block touching one heat source. */
    public static final double PER_SOURCE = 0.03;
    public static final int MAX_SOURCES = 6;

    private HeatMath() {}

    public static double amount(int adjacentHeatSources) {
        int clamped = Math.max(0, Math.min(MAX_SOURCES, adjacentHeatSources));
        return PER_SOURCE * clamped;
    }
}
