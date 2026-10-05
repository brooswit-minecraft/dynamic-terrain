package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EntityMathTest {
    @Test
    void standingOrInvalidReportsNothing() {
        assertEquals(0.0, EntityMath.amount(100, 0.2), 0);
        assertEquals(0.0, EntityMath.amount(0, 5), 0);
        assertEquals(0.0, EntityMath.amount(100, Double.NaN), 0);
    }

    @Test
    void referenceEntityGivesTheBase() {
        assertEquals(EntityMath.BASE, EntityMath.amount(100, 4), 1e-12);
    }

    @Test
    void heavierAndFasterWearMore() {
        assertTrue(EntityMath.amount(500, 4) > EntityMath.amount(100, 4));
        assertTrue(EntityMath.amount(100, 8) > EntityMath.amount(100, 4));
    }

    @Test
    void hugeEntitiesAreBounded() {
        assertEquals(EntityMath.BASE * EntityMath.MAX_FACTOR, EntityMath.amount(1e9, 1e9), 1e-12);
    }

    @Test
    void playerSizedBoxIsAboutSixtyFiveKilograms() {
        assertEquals(64.8, EntityMath.massKg(0.6, 1.8), 0.5);
    }
}
