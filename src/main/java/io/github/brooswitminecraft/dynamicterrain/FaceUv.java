package io.github.brooswitminecraft.dynamicterrain;

/**
 * Turns a contact point in block-local space (each coordinate 0..1 across
 * the block) into the 0..1 texture-space UV on the face it touches, using
 * vanilla's own per-face UV convention. This is what lets a tire crossing a
 * face sample the texels it actually visits instead of an arbitrary
 * world-aligned grid, and is what makes directional grain (travelling along
 * wood grain versus crossing it) emerge naturally from the texture alone.
 */
public final class FaceUv {
    private FaceUv() {}

    public record Uv(double u, double v) {}

    public static Uv of(Face face, double localX, double localY, double localZ) {
        return switch (face) {
            case UP -> new Uv(localX, 1 - localZ);
            case DOWN -> new Uv(localX, localZ);
            case NORTH -> new Uv(1 - localX, 1 - localY);
            case SOUTH -> new Uv(localX, 1 - localY);
            case WEST -> new Uv(localZ, 1 - localY);
            case EAST -> new Uv(1 - localZ, 1 - localY);
        };
    }
}
