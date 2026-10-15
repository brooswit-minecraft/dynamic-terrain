package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SlipMathTest {
    @Test
    void noSlipOrNoLoadReportsNothing() {
        assertEquals(0.0, SlipMath.amount(0, 1000), 0);
        assertEquals(0.0, SlipMath.amount(-5, 1000), 0);
        assertEquals(0.0, SlipMath.amount(10, 0), 0);
        assertEquals(0.0, SlipMath.amount(Double.NaN, 1000), 0);
    }

    @Test
    void referenceSlipAndLoadGiveTheBase() {
        assertEquals(SlipMath.BASE, SlipMath.amount(10, SlipMath.REFERENCE_LOAD_KG), 1e-12);
    }

    @Test
    void harderSlipAndHeavierWheelsWearMore() {
        assertTrue(SlipMath.amount(20, SlipMath.REFERENCE_LOAD_KG) > SlipMath.amount(10, SlipMath.REFERENCE_LOAD_KG));
        assertTrue(SlipMath.amount(10, 2 * SlipMath.REFERENCE_LOAD_KG) > SlipMath.amount(10, SlipMath.REFERENCE_LOAD_KG));
    }

    @Test
    void extremesAreBounded() {
        assertEquals(SlipMath.BASE * 3 * 4, SlipMath.amount(1e9, 1e9), 1e-12);
        assertEquals(SlipMath.BASE * 0.1 * 0.25, SlipMath.amount(1, 1), 1e-12);
    }
}
