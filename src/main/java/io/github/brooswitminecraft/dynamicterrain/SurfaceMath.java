package io.github.brooswitminecraft.dynamicterrain;

/**
 * Pure inference of {@link SurfaceProperties} from what Minecraft already
 * knows about a block: its sound group (a material prior), vanilla friction
 * (slipperiness), hardness and blast resistance. This is the automatic level;
 * SoundType-level and per-block overrides layer on top of it. The tune is a
 * starting point to calibrate against vanilla surfaces.
 */
public final class SurfaceMath {
    /** Coarse material families derived from the SoundType. */
    public enum Family {
        STONE, METAL, WOOD, GLASS, GRAVEL, SAND, GRASS, WOOL, SNOW, SLIME, HONEY, ICE, OTHER
    }

    /** Vanilla's default block friction. Higher means more slippery. */
    public static final double NORMAL_FRICTION = 0.6;

    private SurfaceMath() {}

    /** Material prior: grip, roughness, rolling resistance, and how soft the material is at zero resistance. */
    private static double[] prior(Family family) {
        return switch (family) {
            case STONE -> new double[] {0.95, 0.20, 0.020, 0.00};
            case METAL -> new double[] {0.85, 0.15, 0.020, 0.00};
            case WOOD -> new double[] {0.75, 0.35, 0.030, 0.05};
            case GLASS -> new double[] {0.60, 0.05, 0.020, 0.00};
            case GRAVEL -> new double[] {0.60, 0.80, 0.080, 0.60};
            case SAND -> new double[] {0.45, 0.35, 0.160, 0.95};
            case GRASS -> new double[] {0.55, 0.65, 0.070, 0.50};
            case WOOL -> new double[] {0.65, 0.45, 0.090, 0.35};
            case SNOW -> new double[] {0.30, 0.25, 0.110, 0.75};
            case SLIME -> new double[] {0.50, 0.10, 0.060, 0.25};
            case HONEY -> new double[] {0.35, 0.10, 0.100, 0.30};
            case ICE -> new double[] {0.90, 0.05, 0.015, 0.00};
            case OTHER -> new double[] {0.70, 0.40, 0.040, 0.15};
        };
    }

    public static SurfaceProperties infer(Family family, double friction, double hardness, double blastResistance) {
        double[] p = prior(family);
        // Slippery blocks (friction above normal) cut grip hard: ice at 0.98 lands near 0.1 of pavement.
        double slip = Math.max(0.0, friction - NORMAL_FRICTION);
        double grip = p[0] * Math.max(0.05, 1.0 - slip * 2.4);
        // Softness: the material's own give, reduced as hardness and blast resistance rise.
        double resistance = ErosionMath.resistance(hardness, blastResistance);
        double deformability = p[3] * (1.0 - resistance);
        return new SurfaceProperties(grip, p[1], p[2], deformability);
    }
}
