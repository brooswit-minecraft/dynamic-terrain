package io.github.brooswitminecraft.dynamicterrain;

/**
 * Pure mapping from an entity walking over a block to an erosion amount,
 * roughly mass times speed. Starting numbers, to be tuned in play.
 */
public final class EntityMath {
    /** Amount per sample for a 100 kg entity moving at 4 m/s. */
    public static final double BASE = 0.002;
    public static final double REFERENCE_MASS_KG = 100.0;
    public static final double REFERENCE_SPEED = 4.0;
    /** Below this ground speed (m/s) an entity is standing, not wearing the ground. */
    public static final double MIN_SPEED = 0.5;
    /** Density used to turn a bounding box volume (m^3) into a rough mass. */
    public static final double DENSITY_KG_PER_M3 = 100.0;
    public static final double MAX_FACTOR = 20.0;

    private EntityMath() {}

    public static double massKg(double width, double height) {
        return Math.max(0.0, width * width * height * DENSITY_KG_PER_M3);
    }

    public static double amount(double massKg, double speed) {
        if (!(massKg > 0) || !(speed >= MIN_SPEED)) {
            return 0.0;
        }
        double factor = (massKg / REFERENCE_MASS_KG) * (speed / REFERENCE_SPEED);
        return BASE * Math.min(MAX_FACTOR, factor);
    }
}
