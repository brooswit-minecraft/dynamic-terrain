package io.github.brooswitminecraft.dynamicterrain;

/**
 * Per-texel surface evidence for one canonical texture. Deliberately not a
 * single number, and not a height map: each texel keeps its own chroma,
 * luminance contrast against its neighbors, and local variation along X and
 * Y, all 0..1. Brightness here is a surface-property signal, never geometry.
 */
public record TexelField(int width, int height, float[] chroma, float[] luminanceContrast,
        float[] xVariation, float[] yVariation) {

    public TexelField {
        int expected = width * height;
        if (chroma.length != expected || luminanceContrast.length != expected
                || xVariation.length != expected || yVariation.length != expected) {
            throw new IllegalArgumentException("texel arrays must have length width*height");
        }
    }

    /** One texel's evidence. */
    public record TexelSample(float chroma, float luminanceContrast, float xVariation, float yVariation) {}

    /** Nearest-texel sample at (u, v) in 0..1 texture space, wrapping like a tiled texture. */
    public TexelSample sample(double u, double v) {
        int x = wrap((int) Math.floor(u * width), width);
        int y = wrap((int) Math.floor(v * height), height);
        int i = y * width + x;
        return new TexelSample(chroma[i], luminanceContrast[i], xVariation[i], yVariation[i]);
    }

    private static int wrap(int v, int size) {
        int m = v % size;
        return m < 0 ? m + size : m;
    }
}
