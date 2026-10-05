package io.github.brooswitminecraft.dynamicterrain;

/**
 * Pure derivation of the support score a material needs, from the same
 * resistance used for erosion: strong materials need little connected mass,
 * weak ones need a lot. Unbreakable materials (bedrock) need none. Starting
 * numbers, to be tuned in play.
 */
public final class SupportMath {
    /** Score a very weak material needs (about 10 sideways-connected cells). */
    public static final int WEAKEST = 30;
    /** Score the strongest breakable material needs. */
    public static final int STRONGEST = 3;

    private SupportMath() {}

    public static int requiredScore(double hardness, double blastResistance) {
        if (hardness < 0) {
            return 0;
        }
        double resistance = ErosionMath.resistance(hardness, blastResistance);
        return (int) Math.round(WEAKEST - (WEAKEST - STRONGEST) * resistance);
    }
}
