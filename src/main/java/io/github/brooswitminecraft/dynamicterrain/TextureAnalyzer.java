package io.github.brooswitminecraft.dynamicterrain;

import java.awt.image.BufferedImage;

/**
 * Per-texel analysis of a canonical block texture. Deliberately does not
 * reduce a 16x16 texture to one number: every texel keeps its own chroma,
 * luminance contrast against its immediate neighbors, and local variation
 * along X and Y, so directional grain (wood) and uniform noise (gravel) read
 * differently from a uniform surface (smooth stone).
 */
public final class TextureAnalyzer {
    private TextureAnalyzer() {}

    public static TexelField analyze(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        float[] luma = new float[w * h];
        float[] chroma = new float[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = image.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int i = y * w + x;
                luma[i] = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                chroma[i] = (max - min) / 255f;
            }
        }

        float[] contrast = new float[w * h];
        float[] xVariation = new float[w * h];
        float[] yVariation = new float[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                float left = luma[y * w + wrap(x - 1, w)];
                float right = luma[y * w + wrap(x + 1, w)];
                float up = luma[wrap(y - 1, h) * w + x];
                float down = luma[wrap(y + 1, h) * w + x];
                xVariation[i] = Math.abs(right - left) / 2f;
                yVariation[i] = Math.abs(down - up) / 2f;
                float neighborAvg = (left + right + up + down) / 4f;
                contrast[i] = Math.min(1f, Math.abs(luma[i] - neighborAvg) * 2f);
            }
        }
        return new TexelField(w, h, chroma, contrast, xVariation, yVariation);
    }

    private static int wrap(int v, int size) {
        int m = v % size;
        return m < 0 ? m + size : m;
    }
}
