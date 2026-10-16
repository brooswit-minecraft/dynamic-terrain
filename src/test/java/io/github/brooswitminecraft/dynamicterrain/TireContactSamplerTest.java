package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TireContactSamplerTest {
    /** 4x4 field whose contrast rises left-to-right by column (0, 1/3, 2/3, 1), everything else flat. */
    private static TexelField columnRamp() {
        float[] contrast = new float[16];
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                contrast[y * 4 + x] = x / 3f;
            }
        }
        return new TexelField(4, 4, new float[16], contrast, new float[16], new float[16]);
    }

    @Test
    void aStationaryPatchSamplesExactlyOneTexel() {
        TexelField field = columnRamp();
        TireContactSampler.ContactPatch patch =
                TireContactSampler.integrate(field, Face.UP, 0.1, 0.5, 0.5, 0.1, 0.5, 0.5);
        TexelField.TexelSample expected = field.sample(0.1, 0.5);
        assertEquals(expected.luminanceContrast(), patch.average().luminanceContrast(), 1e-6);
        assertEquals(expected.luminanceContrast(), patch.peak().luminanceContrast(), 1e-6);
    }

    @Test
    void peakContrastIsAtLeastTheAverage() {
        TexelField field = columnRamp();
        TireContactSampler.ContactPatch patch =
                TireContactSampler.integrate(field, Face.UP, 0.0, 0.5, 0.5, 1.0, 0.5, 0.5);
        assertTrue(patch.peak().luminanceContrast() >= patch.average().luminanceContrast());
    }

    @Test
    void crossingTheFullRampAveragesNearItsMidpoint() {
        TexelField field = columnRamp();
        // UP maps (localX, localZ) -> (u, 1 - localZ); sweep the full width at fixed z.
        TireContactSampler.ContactPatch patch =
                TireContactSampler.integrate(field, Face.UP, 0.0, 0.5, 0.5, 1.0, 0.5, 0.5);
        assertEquals(0.5, patch.average().luminanceContrast(), 0.1);
    }
}
