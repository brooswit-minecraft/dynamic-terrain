package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TexelFieldTest {
    private static TexelField twoByTwo() {
        // texel (x,y): index y*2+x. Give each a distinct chroma so sampling can be told apart.
        return new TexelField(2, 2,
                new float[] {0.0f, 0.25f, 0.5f, 0.75f},
                new float[] {0, 0, 0, 0},
                new float[] {0, 0, 0, 0},
                new float[] {0, 0, 0, 0});
    }

    @Test
    void mismatchedArrayLengthIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new TexelField(2, 2, new float[] {0}, new float[4], new float[4], new float[4]));
    }

    @Test
    void sampleReadsTheNearestTexel() {
        TexelField field = twoByTwo();
        assertEquals(0.0f, field.sample(0.0, 0.0).chroma(), 1e-6);
        assertEquals(0.25f, field.sample(0.9, 0.0).chroma(), 1e-6);
        assertEquals(0.5f, field.sample(0.0, 0.9).chroma(), 1e-6);
        assertEquals(0.75f, field.sample(0.9, 0.9).chroma(), 1e-6);
    }

    @Test
    void sampleWrapsOutOfRangeCoordinates() {
        TexelField field = twoByTwo();
        // u=1.0 wraps to x=0, same as u=0.0; a negative coordinate wraps forward instead of clamping.
        assertEquals(field.sample(0.0, 0.0).chroma(), field.sample(1.0, 0.0).chroma(), 1e-6);
        assertEquals(field.sample(0.9, 0.0).chroma(), field.sample(-0.1, 0.0).chroma(), 1e-6);
    }
}
