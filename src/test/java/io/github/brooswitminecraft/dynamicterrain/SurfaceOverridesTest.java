package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Exercises {@link SurfaceOverrides#resolve} through add/change/remove reloads, not just a single snapshot. */
class SurfaceOverridesTest {
    private static final SurfaceProperties BLOCK_DECLARED = new SurfaceProperties(0.9, 0.1, 0.02, 0.0);
    private static final SurfaceProperties FAMILY_DECLARED = new SurfaceProperties(0.2, 0.2, 0.2, 0.2);
    private static final SurfaceProperties CHANGED = new SurfaceProperties(0.5, 0.5, 0.5, 0.5);

    @AfterEach
    void resetTable() {
        SurfaceOverrides.setTable(new SurfaceOverrideTable());
    }

    @Test
    void blockDeclarationWinsOverFamily() {
        SurfaceOverrideTable table = new SurfaceOverrideTable();
        table.putBlock("modid:rubber_pad", BLOCK_DECLARED);
        table.putFamily("stone", FAMILY_DECLARED);
        SurfaceOverrides.setTable(table);
        assertEquals(BLOCK_DECLARED, SurfaceOverrides.resolve("modid:rubber_pad", "STONE").orElseThrow());
    }

    @Test
    void familyAppliesWhenBlockIsNotDeclared() {
        SurfaceOverrideTable table = new SurfaceOverrideTable();
        table.putFamily("stone", FAMILY_DECLARED);
        SurfaceOverrides.setTable(table);
        assertEquals(FAMILY_DECLARED, SurfaceOverrides.resolve("modid:some_other_block", "STONE").orElseThrow());
    }

    @Test
    void neitherDeclaredFallsThroughToAutomaticInference() {
        SurfaceOverrides.setTable(new SurfaceOverrideTable());
        assertTrue(SurfaceOverrides.resolve("modid:undeclared", "STONE").isEmpty());
    }

    @Test
    void addingAnOverrideIsVisibleImmediately() {
        assertTrue(SurfaceOverrides.resolve("modid:rubber_pad", "STONE").isEmpty());
        SurfaceOverrideTable table = new SurfaceOverrideTable();
        table.putBlock("modid:rubber_pad", BLOCK_DECLARED);
        SurfaceOverrides.setTable(table);
        assertEquals(BLOCK_DECLARED, SurfaceOverrides.resolve("modid:rubber_pad", "STONE").orElseThrow());
    }

    @Test
    void changingAnOverrideReplacesThePreviousValueOnReload() {
        SurfaceOverrideTable first = new SurfaceOverrideTable();
        first.putBlock("modid:rubber_pad", BLOCK_DECLARED);
        SurfaceOverrides.setTable(first);
        assertEquals(BLOCK_DECLARED, SurfaceOverrides.resolve("modid:rubber_pad", "STONE").orElseThrow());

        SurfaceOverrideTable second = new SurfaceOverrideTable();
        second.putBlock("modid:rubber_pad", CHANGED);
        SurfaceOverrides.setTable(second);
        assertEquals(CHANGED, SurfaceOverrides.resolve("modid:rubber_pad", "STONE").orElseThrow());
    }

    @Test
    void removingAnOverrideFallsBackToEmptyOnReload() {
        SurfaceOverrideTable declared = new SurfaceOverrideTable();
        declared.putBlock("modid:rubber_pad", BLOCK_DECLARED);
        SurfaceOverrides.setTable(declared);
        assertEquals(BLOCK_DECLARED, SurfaceOverrides.resolve("modid:rubber_pad", "STONE").orElseThrow());

        SurfaceOverrides.setTable(new SurfaceOverrideTable());
        assertTrue(SurfaceOverrides.resolve("modid:rubber_pad", "STONE").isEmpty());
    }
}
