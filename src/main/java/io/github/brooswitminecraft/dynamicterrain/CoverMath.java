package io.github.brooswitminecraft.dynamicterrain;

/**
 * Pure rules for the snow layer that may sit directly above a smoothed ground column. Minecraft-free so
 * it can be unit tested without bootstrapping the game; {@link SmoothSurfaceFeature} is the thin glue that
 * reads and writes the actual blocks these describe.
 */
public final class CoverMath {
    private CoverMath() {}

    /** What occupies the cell directly above a candidate ground column, as far as worldgen smoothing cares. */
    public enum Kind { AIR, SNOW, OTHER }

    /**
     * Whether a column with this cover may still be smoothed. Only bare ground or a thin snow layer
     * qualify; anything else (a tree, a plant, water, ...) means the column isn't plain natural ground.
     */
    public static boolean eligible(Kind kind) {
        return kind != Kind.OTHER;
    }

    /**
     * Where the cover cell ends up once the ground surface is replaced by {@code cell}: one cell above
     * a ground cell that ends up holding any material, or the cell itself when it ends up empty. The
     * cover started out directly above the original surface; it only actually moves when this differs
     * from that original height.
     */
    public static int coverY(SurfaceSmoother.Cell cell) {
        return cell.layers() > 0 ? cell.y() + 1 : cell.y();
    }
}
