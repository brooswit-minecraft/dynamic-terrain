package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HeadingTest {
    @Test
    void pullsTowardTheDominantAxisOfThePlayer() {
        assertEquals(Heading.EAST, Heading.toward(2.0, 0.5));
        assertEquals(Heading.WEST, Heading.toward(-2.0, 0.5));
        assertEquals(Heading.SOUTH, Heading.toward(0.5, 2.0));
        assertEquals(Heading.NORTH, Heading.toward(0.5, -2.0));
    }

    @Test
    void tiesGoToZ() {
        assertEquals(Heading.SOUTH, Heading.toward(1.0, 1.0));
        assertEquals(Heading.NORTH, Heading.toward(-1.0, -1.0));
    }
}
