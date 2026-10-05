package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SupportMathTest {
    @Test
    void strongerMaterialsNeedLessSupport() {
        int dirt = SupportMath.requiredScore(0.5, 0.5);
        int stone = SupportMath.requiredScore(1.5, 6.0);
        int deepslate = SupportMath.requiredScore(3.0, 6.0);
        int obsidian = SupportMath.requiredScore(50.0, 1200.0);
        assertTrue(dirt > stone && stone > deepslate && deepslate > obsidian);
    }

    @Test
    void extremesAreBounded() {
        assertEquals(SupportMath.STRONGEST, SupportMath.requiredScore(50.0, 1200.0));
        assertEquals(SupportMath.STRONGEST, SupportMath.requiredScore(9999.0, 3_600_000.0));
        assertTrue(SupportMath.requiredScore(0.0, 0.0) <= SupportMath.WEAKEST);
        assertTrue(SupportMath.requiredScore(0.0, 0.0) > SupportMath.requiredScore(1.5, 6.0));
    }

    @Test
    void unbreakableMaterialNeedsNoSupport() {
        assertEquals(0, SupportMath.requiredScore(-1.0, 3_600_000.0));
    }
}
