package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

/** Per-texel analysis against hand-built textures with a known answer. */
class TextureAnalyzerTest {
    private static BufferedImage image(int w, int h, int[][] argb) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                img.setRGB(x, y, argb[y][x]);
            }
        }
        return img;
    }

    private static int gray(int v) {
        return 0xFF000000 | (v << 16) | (v << 8) | v;
    }

    @Test
    void uniformGrayHasNoChromaContrastOrVariation() {
        int[][] rows = new int[4][4];
        for (int[] row : rows) {
            java.util.Arrays.fill(row, gray(128));
        }
        TexelField field = TextureAnalyzer.analyze(image(4, 4, rows));
        for (float v : field.chroma()) assertEquals(0f, v, 1e-6);
        for (float v : field.luminanceContrast()) assertEquals(0f, v, 1e-6);
        for (float v : field.xVariation()) assertEquals(0f, v, 1e-6);
        for (float v : field.yVariation()) assertEquals(0f, v, 1e-6);
    }

    @Test
    void pureRedIsFullyColorfulButStillUniform() {
        int[][] rows = new int[4][4];
        for (int[] row : rows) {
            java.util.Arrays.fill(row, 0xFFFF0000);
        }
        TexelField field = TextureAnalyzer.analyze(image(4, 4, rows));
        for (float v : field.chroma()) assertEquals(1f, v, 1e-6);
        for (float v : field.luminanceContrast()) assertEquals(0f, v, 1e-6);
    }

    @Test
    void verticalBandsVaryAlongXNotY() {
        // 2px-wide columns alternate black/white; each column is uniform top-to-bottom. (1px-wide
        // alternation would alias to zero under a centered x-1/x+1 difference, so bands are 2px wide.)
        int[][] rows = new int[4][4];
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                rows[y][x] = gray((x / 2) % 2 == 0 ? 0 : 255);
            }
        }
        TexelField field = TextureAnalyzer.analyze(image(4, 4, rows));
        for (float v : field.xVariation()) assertTrue(v > 0.4f, "expected strong X variation: " + v);
        for (float v : field.yVariation()) assertEquals(0f, v, 1e-6);
    }

    @Test
    void horizontalBandsVaryAlongYNotX() {
        int[][] rows = new int[4][4];
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                rows[y][x] = gray((y / 2) % 2 == 0 ? 0 : 255);
            }
        }
        TexelField field = TextureAnalyzer.analyze(image(4, 4, rows));
        for (float v : field.yVariation()) assertTrue(v > 0.4f, "expected strong Y variation: " + v);
        for (float v : field.xVariation()) assertEquals(0f, v, 1e-6);
    }

    @Test
    void checkerboardHasHighContrastAgainstSmoothNeighbors() {
        int[][] checker = new int[4][4];
        int[][] uniform = new int[4][4];
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                checker[y][x] = gray((x + y) % 2 == 0 ? 0 : 255);
                uniform[y][x] = gray(128);
            }
        }
        TexelField checkerField = TextureAnalyzer.analyze(image(4, 4, checker));
        TexelField uniformField = TextureAnalyzer.analyze(image(4, 4, uniform));
        for (float v : checkerField.luminanceContrast()) assertTrue(v > 0.9f, "expected near-max contrast: " + v);
        for (float v : uniformField.luminanceContrast()) assertEquals(0f, v, 1e-6);
    }
}
