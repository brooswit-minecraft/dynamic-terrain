package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.brooswitminecraft.dynamicterrain.SurfaceMath.Family;

/** Calibration against vanilla surfaces (friction, hardness, blast resistance as in vanilla). */
class SurfaceMathTest {
    private static SurfaceProperties grass() { return SurfaceMath.infer(Family.GRASS, 0.6, 0.6, 0.6); }
    private static SurfaceProperties dirt() { return SurfaceMath.infer(Family.GRAVEL, 0.6, 0.5, 0.5); }
    private static SurfaceProperties sand() { return SurfaceMath.infer(Family.SAND, 0.6, 0.5, 0.5); }
    private static SurfaceProperties gravel() { return SurfaceMath.infer(Family.GRAVEL, 0.6, 0.6, 0.6); }
    private static SurfaceProperties stone() { return SurfaceMath.infer(Family.STONE, 0.6, 1.5, 6.0); }
    private static SurfaceProperties smoothStone() { return SurfaceMath.infer(Family.STONE, 0.6, 2.0, 6.0); }
    private static SurfaceProperties deepslate() { return SurfaceMath.infer(Family.STONE, 0.6, 3.0, 6.0); }
    private static SurfaceProperties ice() { return SurfaceMath.infer(Family.ICE, 0.98, 0.5, 0.5); }
    private static SurfaceProperties blueIce() { return SurfaceMath.infer(Family.ICE, 0.989, 2.8, 2.8); }
    private static SurfaceProperties obsidian() { return SurfaceMath.infer(Family.STONE, 0.6, 50.0, 1200.0); }
    private static SurfaceProperties slime() { return SurfaceMath.infer(Family.SLIME, 0.8, 0.0, 0.0); }
    private static SurfaceProperties wool() { return SurfaceMath.infer(Family.WOOL, 0.6, 0.8, 0.8); }

    @Test
    void gripOrderingMatchesTheTargetFeel() {
        assertTrue(ice().grip() < 0.15, "ice is hard and low-grip: " + ice().grip());
        assertTrue(blueIce().grip() <= ice().grip());
        assertTrue(ice().grip() < sand().grip());
        assertTrue(sand().grip() < grass().grip());
        assertTrue(grass().grip() < gravel().grip());
        assertTrue(gravel().grip() < stone().grip());
        assertTrue(slime().grip() < stone().grip());
    }

    @Test
    void sandIsHighlyLossyAndDeformable() {
        assertTrue(sand().deformability() > 0.8);
        assertTrue(sand().rollingResistance() > dirt().rollingResistance());
        assertTrue(sand().rollingResistance() > stone().rollingResistance() * 5);
    }

    @Test
    void gravelIsNoisyAndRough() {
        assertTrue(gravel().roughness() > grass().roughness());
        assertTrue(gravel().roughness() > 3 * smoothStone().roughness() + 0.1);
    }

    @Test
    void dirtAndGrassAreSoftButNotSand() {
        assertTrue(dirt().deformability() > 0.2 && dirt().deformability() < sand().deformability());
        assertTrue(grass().deformability() > 0.2 && grass().deformability() < sand().deformability());
    }

    @Test
    void stoneIsConsistentPavementAndDeepslateIsAtLeastAsDurable() {
        assertEquals(0.0, stone().deformability(), 1e-9);
        assertEquals(0.0, deepslate().deformability(), 1e-9);
        assertTrue(smoothStone().roughness() <= gravel().roughness());
        assertTrue(deepslate().grip() >= 0.9);
    }

    @Test
    void obsidianIsPavementNotAnOutlier() {
        // Superficially stone (same sound group and friction) with extreme hardness: it must not become
        // glass-slick or absurdly grippy, it must read as hard pavement like deepslate.
        assertEquals(stone().grip(), obsidian().grip(), 1e-9);
        assertEquals(0.0, obsidian().deformability(), 1e-9);
        assertEquals(deepslate().rollingResistance(), obsidian().rollingResistance(), 1e-9);
    }

    @Test
    void woolIsSoftishAndMoreResistiveThanStone() {
        assertTrue(wool().deformability() > 0.0);
        assertTrue(wool().rollingResistance() > stone().rollingResistance());
    }

    @Test
    void allValuesStayInRange() {
        for (Family f : Family.values()) {
            for (double friction : new double[] {0.0, 0.6, 0.98, 5.0}) {
                SurfaceProperties s = SurfaceMath.infer(f, friction, -1.0, 1e9);
                for (double v : new double[] {s.grip(), s.roughness(), s.rollingResistance(), s.deformability()}) {
                    assertTrue(v >= 0.0 && v <= 1.0, f + " " + friction + " -> " + v);
                }
            }
        }
    }
}
