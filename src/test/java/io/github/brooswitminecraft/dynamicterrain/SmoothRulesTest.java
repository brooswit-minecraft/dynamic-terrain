package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.brooswitminecraft.dynamicterrain.SmoothRules.Cell;
import io.github.brooswitminecraft.dynamicterrain.SmoothRules.Destination;
import io.github.brooswitminecraft.dynamicterrain.SmoothRules.Plan;

class SmoothRulesTest {
    private static final Cell FULL = new Cell(16, false);

    @Test
    void fullBlockIntoSupportedEmptyBecomesFifteenAndOne() {
        Plan plan = SmoothRules.plan(FULL, Destination.EMPTY, null, true).orElseThrow();
        assertEquals(new Cell(15, false), plan.source());
        assertEquals(new Cell(1, false), plan.destination());
    }

    @Test
    void emptyDestinationWithoutAnchorSupportRejects() {
        assertTrue(SmoothRules.plan(FULL, Destination.EMPTY, null, false).isEmpty());
    }

    @Test
    void lastLayerLeavesTheSourceEmpty() {
        Plan plan = SmoothRules.plan(new Cell(1, false), Destination.EMPTY, null, true).orElseThrow();
        assertNull(plan.source());
    }

    @Test
    void sameMaterialDestinationGainsALayer() {
        Plan plan = SmoothRules.plan(new Cell(5, false), Destination.SAME_MATERIAL, new Cell(3, false), false).orElseThrow();
        assertEquals(new Cell(4, false), plan.source());
        assertEquals(new Cell(4, false), plan.destination());
    }

    @Test
    void fullDestinationRejects() {
        assertTrue(SmoothRules.plan(new Cell(5, false), Destination.SAME_MATERIAL, FULL, true).isEmpty());
    }

    @Test
    void mismatchedAnchorRejects() {
        assertTrue(SmoothRules.plan(new Cell(5, false), Destination.SAME_MATERIAL, new Cell(3, true), true).isEmpty());
    }

    @Test
    void blockedDestinationRejects() {
        assertTrue(SmoothRules.plan(FULL, Destination.BLOCKED, null, true).isEmpty());
    }

    @Test
    void ceilingLayersStayCeilingAnchored() {
        Plan plan = SmoothRules.plan(new Cell(4, true), Destination.EMPTY, null, true).orElseThrow();
        assertEquals(new Cell(3, true), plan.source());
        assertEquals(new Cell(1, true), plan.destination());
    }

    @Test
    void movingConservesMaterial() {
        Plan plan = SmoothRules.plan(new Cell(7, false), Destination.SAME_MATERIAL, new Cell(9, false), false).orElseThrow();
        assertEquals(16, plan.source().layers() + plan.destination().layers());
    }
}
