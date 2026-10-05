package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WaterMathTest {
    @Test
    void stillWaterErodesFarLessThanMovingWater() {
        assertTrue(WaterMath.amount(false, 8) > 10 * WaterMath.amount(true, 8));
    }

    @Test
    void shallowerFlowErodesLess() {
        assertTrue(WaterMath.amount(false, 8) > WaterMath.amount(false, 4));
        assertTrue(WaterMath.amount(false, 4) > WaterMath.amount(false, 1));
    }

    @Test
    void sourceBlocksIgnoreTheFluidAmount() {
        assertEquals(WaterMath.amount(true, 8), WaterMath.amount(true, 3), 1e-12);
    }

    @Test
    void outOfRangeAmountsAreClamped() {
        assertEquals(WaterMath.MOVING, WaterMath.amount(false, 99), 1e-12);
        assertEquals(0.0, WaterMath.amount(false, -3), 1e-12);
    }
}
