package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.brooswitminecraft.dynamicterrain.SmoothRules.Cell;
import io.github.brooswitminecraft.dynamicterrain.SmoothRules.Destination;
import io.github.brooswitminecraft.dynamicterrain.SmoothRules.Plan;

/**
 * Covers MINECRAFT-383: a successful erosion move settles grass as dirt on
 * both ends; manual grading and anything that leaves {@code disturb} off
 * does not. {@link Settling#settle} is the exact decision Smoothing.smooth()
 * uses, so these tests exercise the real production seam without needing a
 * Minecraft-bootstrapped Level (this repo has no such harness anywhere).
 */
class SettlingTest {
    private static final Cell FULL = new Cell(16, false);

    @Test
    void erosionOptInSettlesWhileManualGradingDoesNot() {
        Settling.Result erosion = Settling.settle(true, true, Destination.SAME_MATERIAL, true);
        Settling.Result manual = Settling.settle(false, true, Destination.SAME_MATERIAL, true);
        assertTrue(erosion.source());
        assertTrue(erosion.destination());
        assertFalse(manual.source());
        assertFalse(manual.destination());
    }

    @Test
    void grassOntoEmptySettlesBothTheRemainingSourceAndTheDeposit() {
        Settling.Result settle = Settling.settle(true, true, Destination.EMPTY, false);
        assertTrue(settle.source());
        assertTrue(settle.destination());
    }

    @Test
    void grassOntoGrassSettlesBothEnds() {
        Settling.Result settle = Settling.settle(true, true, Destination.SAME_MATERIAL, true);
        assertTrue(settle.source());
        assertTrue(settle.destination());
    }

    @Test
    void dirtOntoGrassSettlesOnlyTheReceiver() {
        Settling.Result settle = Settling.settle(true, false, Destination.SAME_MATERIAL, true);
        assertFalse(settle.source());
        assertTrue(settle.destination());
    }

    @Test
    void grassOntoDirtSettlesOnlyTheSource() {
        Settling.Result settle = Settling.settle(true, true, Destination.SAME_MATERIAL, false);
        assertTrue(settle.source());
        assertFalse(settle.destination());
    }

    @Test
    void nonGrassSoilMovingOntoNonGrassSoilSettlesNeither() {
        Settling.Result settle = Settling.settle(true, false, Destination.SAME_MATERIAL, false);
        assertFalse(settle.source());
        assertFalse(settle.destination());
    }

    @Test
    void blockedDestinationNeverSettles() {
        Settling.Result settle = Settling.settle(true, true, Destination.BLOCKED, false);
        assertFalse(settle.destination());
    }

    @Test
    void erosionDisturbedFullGrassOntoEmptySettlesBothEndsAndConservesLayers() {
        Plan plan = SmoothRules.plan(FULL, Destination.EMPTY, null, true).orElseThrow();
        Settling.Result settle = Settling.settle(true, true, Destination.EMPTY, false);
        assertEquals(new Cell(15, false), plan.source());
        assertEquals(new Cell(1, false), plan.destination());
        assertEquals(16, plan.source().layers() + plan.destination().layers());
        assertTrue(settle.source());
        assertTrue(settle.destination());
    }

    @Test
    void depletedGrassSourceBecomesAirRegardlessOfSettling() {
        Plan plan = SmoothRules.plan(new Cell(1, false), Destination.EMPTY, null, true).orElseThrow();
        assertNull(plan.source());
    }

    @Test
    void rejectedMoveNeverReachesSettling() {
        assertTrue(SmoothRules.plan(FULL, Destination.BLOCKED, null, true).isEmpty());
        assertTrue(SmoothRules.plan(FULL, Destination.EMPTY, null, false).isEmpty());
    }
}
