package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HeatMathTest {
    @Test
    void noHeatSourcesMeansNoErosion() {
        assertEquals(0.0, HeatMath.amount(0), 0);
        assertEquals(0.0, HeatMath.amount(-2), 0);
    }

    @Test
    void moreAdjacentSourcesWearMore() {
        assertTrue(HeatMath.amount(3) > HeatMath.amount(1));
    }

    @Test
    void amountIsBoundedBySixFaces() {
        assertEquals(HeatMath.amount(6), HeatMath.amount(60), 1e-12);
    }
}
