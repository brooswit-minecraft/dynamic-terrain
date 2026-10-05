package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.github.brooswitminecraft.dynamicterrain.SupportSearch.Cell;
import io.github.brooswitminecraft.dynamicterrain.SupportSearch.Grid;

class SupportSearchTest {
    private static Grid solids(int[]... cells) {
        Set<String> set = new HashSet<>();
        for (int[] c : cells) {
            set.add(c[0] + "," + c[1] + "," + c[2]);
        }
        return (x, y, z) -> set.contains(x + "," + y + "," + z) ? Cell.SOLID : Cell.EMPTY;
    }

    @Test
    void aloneBlockHasNoSupport() {
        assertFalse(SupportSearch.isSupported(solids(), 0, 0, 0, 1, 100));
    }

    @Test
    void zeroThresholdIsAlwaysSupported() {
        assertTrue(SupportSearch.isSupported(solids(), 0, 0, 0, 0, 100));
    }

    @Test
    void weightsFollowTheConnectionDirection() {
        // Start at the origin. One solid below is worth 3, beside 2, above 1.
        assertTrue(SupportSearch.isSupported(solids(new int[] {0, -1, 0}), 0, 0, 0, 3, 100));
        assertFalse(SupportSearch.isSupported(solids(new int[] {0, -1, 0}), 0, 0, 0, 4, 100));
        assertTrue(SupportSearch.isSupported(solids(new int[] {1, 0, 0}), 0, 0, 0, 2, 100));
        assertFalse(SupportSearch.isSupported(solids(new int[] {1, 0, 0}), 0, 0, 0, 3, 100));
        assertTrue(SupportSearch.isSupported(solids(new int[] {0, 1, 0}), 0, 0, 0, 1, 100));
        assertFalse(SupportSearch.isSupported(solids(new int[] {0, 1, 0}), 0, 0, 0, 2, 100));
    }

    @Test
    void anAnchorSupportsFullyWhereverItIs() {
        Grid grid = (x, y, z) -> y == -3 ? Cell.ANCHOR : (y > -3 && y <= 0 ? Cell.SOLID : Cell.EMPTY);
        assertTrue(SupportSearch.isSupported(grid, 0, 0, 0, 1000, 100));
    }

    @Test
    void aLargeFloatingMassHoldsItselfUp() {
        // A 5x5x5 cube floating in air: the middle cell finds plenty of connected mass.
        Grid cube = (x, y, z) -> Math.abs(x) <= 2 && Math.abs(y) <= 2 && Math.abs(z) <= 2 ? Cell.SOLID : Cell.EMPTY;
        assertTrue(SupportSearch.isSupported(cube, 0, 0, 0, 20, 1000));
        // ...but a thin floating bar of the same threshold does not.
        Grid bar = (x, y, z) -> y == 0 && z == 0 && x >= 0 && x <= 1 ? Cell.SOLID : Cell.EMPTY;
        assertFalse(SupportSearch.isSupported(bar, 0, 0, 0, 20, 1000));
    }

    @Test
    void searchStopsAsSoonAsTheThresholdIsMet() {
        AtomicInteger reads = new AtomicInteger();
        Grid wall = (x, y, z) -> {
            reads.incrementAndGet();
            return Cell.SOLID;
        };
        assertTrue(SupportSearch.isSupported(wall, 0, 0, 0, 3, 100_000));
        assertTrue(reads.get() < 50, "read " + reads.get() + " cells for a threshold of 3");
    }

    @Test
    void visitedCapBoundsTheSearchAndFailsClosed() {
        Grid endless = (x, y, z) -> Cell.SOLID;
        AtomicInteger reads = new AtomicInteger();
        Grid counting = (x, y, z) -> {
            reads.incrementAndGet();
            return endless.at(x, y, z);
        };
        assertFalse(SupportSearch.isSupported(counting, 0, 0, 0, Integer.MAX_VALUE, 40));
        assertTrue(reads.get() < 400, "cap did not bound the search: " + reads.get());
    }

    @Test
    void loopsDoNotCountACellTwice() {
        // A 2x1x2 ring of four solids at y=0 around the start: sideways cells are
        // reachable by several paths but each counts once (2 each), so 4 neighbours
        // reached directly or via loops never exceed what each cell is worth once.
        Grid ring = solids(new int[] {1, 0, 0}, new int[] {1, 0, 1}, new int[] {0, 0, 1});
        // (1,0,0)=side 2, (0,0,1)=side 2, (1,0,1) reached from a neighbour = side 2 -> 6 total.
        assertTrue(SupportSearch.isSupported(ring, 0, 0, 0, 6, 100));
        assertFalse(SupportSearch.isSupported(ring, 0, 0, 0, 7, 100));
        assertEquals(true, SupportSearch.isSupported(ring, 0, 0, 0, 6, 100));
    }
}
