package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.brooswitminecraft.dynamicterrain.SurfaceSmoother.Cell;

class CoverMathTest {
    @Test
    void airAndSnowAreEligibleOtherIsNot() {
        assertTrue(CoverMath.eligible(CoverMath.Kind.AIR));
        assertTrue(CoverMath.eligible(CoverMath.Kind.SNOW));
        assertFalse(CoverMath.eligible(CoverMath.Kind.OTHER));
    }

    @Test
    void risingGroundPushesCoverUpOneCell() {
        // surface=70: the ground rises into a partial block at y=70 (12/16 full), so the cover that was
        // sitting at 70 is consumed by the new ground and has to move up to 71.
        assertEquals(71, CoverMath.coverY(new Cell(70, 12)));
    }

    @Test
    void droppingGroundWithAPartialCellLeavesCoverInPlace() {
        // surface=70: the ground drops but keeps a partial block at y=69 (5/16 full); the cover at 70
        // is still directly above a filled cell, so it doesn't move.
        assertEquals(70, CoverMath.coverY(new Cell(69, 5)));
    }

    @Test
    void droppingGroundToEmptyPullsCoverDownOneCell() {
        // surface=70: the former top cell at y=69 is now fully empty, so the cover drops down to sit
        // directly on the next solid cell, at 69.
        assertEquals(69, CoverMath.coverY(new Cell(69, 0)));
    }

    @Test
    void unchangedSurfaceLeavesCoverInPlace() {
        // surface=70, unmoved: cell (69, 16) is the same physical surface as the original flat ground.
        assertEquals(70, CoverMath.coverY(new Cell(69, 16)));
    }
}
