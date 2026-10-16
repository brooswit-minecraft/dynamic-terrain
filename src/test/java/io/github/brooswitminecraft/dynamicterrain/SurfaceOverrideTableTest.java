package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SurfaceOverrideTableTest {
    private static final SurfaceProperties RUBBER = new SurfaceProperties(0.9, 0.2, 0.03, 0.05);
    private static final SurfaceProperties SLICK_METAL = new SurfaceProperties(0.4, 0.05, 0.01, 0.0);

    @Test
    void declaredBlockResolves() {
        SurfaceOverrideTable table = new SurfaceOverrideTable();
        table.putBlock("modid:rubber_pad", RUBBER);
        assertEquals(RUBBER, table.forBlock("modid:rubber_pad").orElseThrow());
    }

    @Test
    void declaredFamilyResolvesCaseInsensitively() {
        SurfaceOverrideTable table = new SurfaceOverrideTable();
        table.putFamily("METAL", SLICK_METAL);
        assertEquals(SLICK_METAL, table.forFamily("metal").orElseThrow());
    }

    @Test
    void unknownBlockOrFamilyIsNoResult() {
        SurfaceOverrideTable table = new SurfaceOverrideTable();
        table.putBlock("modid:rubber_pad", RUBBER);
        assertTrue(table.forBlock("modid:other").isEmpty());
        assertTrue(table.forFamily("stone").isEmpty());
    }

    @Test
    void laterPutWins() {
        SurfaceOverrideTable table = new SurfaceOverrideTable();
        table.putBlock("modid:rubber_pad", RUBBER);
        table.putBlock("modid:rubber_pad", SLICK_METAL);
        assertEquals(SLICK_METAL, table.forBlock("modid:rubber_pad").orElseThrow());
        assertEquals(1, table.size());
    }

    @Test
    void blockAndFamilyAreIndependent() {
        SurfaceOverrideTable table = new SurfaceOverrideTable();
        table.putBlock("modid:rubber_pad", RUBBER);
        table.putFamily("metal", SLICK_METAL);
        assertEquals(2, table.size());
    }
}
