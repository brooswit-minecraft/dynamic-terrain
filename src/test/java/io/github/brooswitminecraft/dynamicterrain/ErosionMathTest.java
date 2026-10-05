package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ErosionMathTest {
    private static double r(double hardness, double blast) {
        return ErosionMath.resistance(hardness, blast);
    }

    @Test
    void softMaterialsResistLessThanHardOnes() {
        double dirt = r(0.5, 0.5);
        double stone = r(1.5, 6.0);
        double deepslate = r(3.0, 6.0);
        double obsidian = r(50.0, 1200.0);
        assertTrue(dirt < stone && stone < deepslate && deepslate < obsidian);
    }

    @Test
    void obsidianAndOutliersAreBoundedNotUnbounded() {
        assertEquals(1.0, r(50.0, 1200.0), 1e-9);
        assertEquals(1.0, r(5000.0, 3_600_000.0), 1e-9);
    }

    @Test
    void unbreakableBlocksNeverErode() {
        double bedrock = r(-1.0, 3_600_000.0);
        assertEquals(1.0, bedrock, 1e-9);
        assertEquals(0.0, ErosionMath.successChance(10.0, bedrock), 1e-9);
    }

    @Test
    void chanceScalesWithAmountAndIsClamped() {
        double soft = r(0.5, 0.5);
        assertTrue(ErosionMath.successChance(0.2, soft) > ErosionMath.successChance(0.1, soft));
        assertEquals(1.0, ErosionMath.successChance(1000.0, soft), 1e-9);
        assertEquals(0.0, ErosionMath.successChance(0.0, soft), 1e-9);
    }

    @Test
    void dirtErodesFarMoreOftenThanDeepslate() {
        assertTrue(ErosionMath.successChance(0.1, r(0.5, 0.5)) > 3 * ErosionMath.successChance(0.1, r(3.0, 6.0)));
    }

    @Test
    void materialOnlyFlowsStrictlyDownhill() {
        assertTrue(ErosionMath.isDownhill(10, 3));
        assertTrue(ErosionMath.isDownhill(1, 0));
        assertFalse(ErosionMath.isDownhill(5, 5));
        assertFalse(ErosionMath.isDownhill(3, 10));
    }
}
