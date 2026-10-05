package io.github.brooswitminecraft.dynamicterrain;

/**
 * Pure mapping from tire slip to an erosion amount. Callers report once per
 * physics tick, so the base is small: a sustained burnout wears dirt in
 * seconds, a brief skid barely scratches it. Starting numbers, to be tuned.
 */
public final class SlipMath {
    /** Erosion amount for 10 m/s of slip under a 1000 kg wheel load, per report. */
    public static final double BASE = 0.01;
    public static final double REFERENCE_SLIP = 10.0;
    public static final double REFERENCE_LOAD_KG = 1000.0;
    public static final double MAX_SLIP_FACTOR = 3.0;
    public static final double MIN_LOAD_FACTOR = 0.25;
    public static final double MAX_LOAD_FACTOR = 4.0;

    private SlipMath() {}

    /**
     * @param slipSpeed speed of the contact patch sliding over the surface, m/s (not wheel speed)
     * @param wheelLoadKg mass pressing on this wheel
     */
    public static double amount(double slipSpeed, double wheelLoadKg) {
        if (!(slipSpeed > 0) || !(wheelLoadKg > 0)) {
            return 0.0;
        }
        double slipFactor = Math.min(MAX_SLIP_FACTOR, slipSpeed / REFERENCE_SLIP);
        double loadFactor = Math.max(MIN_LOAD_FACTOR, Math.min(MAX_LOAD_FACTOR, wheelLoadKg / REFERENCE_LOAD_KG));
        return BASE * slipFactor * loadFactor;
    }
}
