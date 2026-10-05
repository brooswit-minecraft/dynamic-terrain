package io.github.brooswitminecraft.dynamicterrain;

/**
 * What a vehicle feels from the surface under a tire. These affect vehicle
 * physics only; persistent world change goes through erosion.
 *
 * @param grip 0..1, share of dry-pavement tire grip available (ice is near 0)
 * @param roughness 0..1, how bumpy and noisy the surface is
 * @param rollingResistance 0..1, extra drag from rolling over it (sand is high, pavement low)
 * @param deformability 0..1, how much the surface gives under load and wheelspin
 */
public record SurfaceProperties(double grip, double roughness, double rollingResistance, double deformability) {
    public SurfaceProperties {
        grip = clamp(grip);
        roughness = clamp(roughness);
        rollingResistance = clamp(rollingResistance);
        deformability = clamp(deformability);
    }

    /** Dry, smooth pavement: the reference surface. */
    public static final SurfaceProperties PAVEMENT = new SurfaceProperties(1.0, 0.1, 0.02, 0.0);

    private static double clamp(double v) {
        return Double.isNaN(v) ? 0.0 : Math.max(0.0, Math.min(1.0, v));
    }
}
