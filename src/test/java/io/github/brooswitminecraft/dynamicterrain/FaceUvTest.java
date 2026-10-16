package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FaceUvTest {
    @Test
    void everyFaceMapsBlockCenterToUvCenter() {
        for (Face face : Face.values()) {
            FaceUv.Uv uv = FaceUv.of(face, 0.5, 0.5, 0.5);
            assertEquals(0.5, uv.u(), 1e-9, face.toString());
            assertEquals(0.5, uv.v(), 1e-9, face.toString());
        }
    }

    @Test
    void upAndDownUseXAndZInOppositeVDirection() {
        FaceUv.Uv up = FaceUv.of(Face.UP, 0.2, 1.0, 0.3);
        FaceUv.Uv down = FaceUv.of(Face.DOWN, 0.2, 0.0, 0.3);
        assertEquals(0.2, up.u(), 1e-9);
        assertEquals(0.7, up.v(), 1e-9);
        assertEquals(0.2, down.u(), 1e-9);
        assertEquals(0.3, down.v(), 1e-9);
    }

    @Test
    void everyCoordinateStaysInUnitRangeForUnitRangeInput() {
        for (Face face : Face.values()) {
            for (double x : new double[] {0.0, 1.0}) {
                for (double y : new double[] {0.0, 1.0}) {
                    for (double z : new double[] {0.0, 1.0}) {
                        FaceUv.Uv uv = FaceUv.of(face, x, y, z);
                        assertEquals(true, uv.u() >= 0.0 && uv.u() <= 1.0, face + " u=" + uv.u());
                        assertEquals(true, uv.v() >= 0.0 && uv.v() <= 1.0, face + " v=" + uv.v());
                    }
                }
            }
        }
    }
}
