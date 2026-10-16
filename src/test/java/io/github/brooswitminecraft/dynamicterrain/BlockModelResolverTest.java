package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Exercises the resolver with injected JSON fixtures shaped like a real log's blockstate/model
 * (vertical vs horizontal axis), rather than real game assets: this stays a pure Java test even
 * though the classpath here has neither Minecraft nor gson (see CLAUDE.md on this repo's test
 * sourceSet). Mirrors the review's ask: state-aware variant matching for a rotated-axis block, an
 * honest no-evidence result instead of a guess when a variant rotates the model, and cache keys
 * that don't let one state's state leak into another's.
 */
class BlockModelResolverTest {
    /** A cube_column-shaped log: vertical (axis=y) is unrotated; horizontal axes rotate the model. */
    private static BlockModelResolver.JsonLoader logFixture() {
        Map<String, Object> documents = new HashMap<>();
        documents.put("minecraft:blockstates/oak_log", MiniJson.parse("""
                {"variants": {
                    "axis=x": {"model": "minecraft:block/oak_log_horizontal", "x": 90, "y": 90},
                    "axis=y": {"model": "minecraft:block/oak_log"},
                    "axis=z": {"model": "minecraft:block/oak_log_horizontal", "x": 90}
                }}
                """));
        documents.put("minecraft:models/block/oak_log", MiniJson.parse("""
                {"parent": "minecraft:block/cube_column",
                 "textures": {"end": "minecraft:block/oak_log_top", "side": "minecraft:block/oak_log"}}
                """));
        documents.put("minecraft:models/block/cube_column", MiniJson.parse("""
                {"elements": [{"faces": {
                    "down": {"texture": "#end"}, "up": {"texture": "#end"},
                    "north": {"texture": "#side"}, "south": {"texture": "#side"},
                    "east": {"texture": "#side"}, "west": {"texture": "#side"}
                }}]}
                """));
        documents.put("minecraft:models/block/oak_log_horizontal", MiniJson.parse("""
                {"parent": "minecraft:block/cube_column",
                 "textures": {"end": "minecraft:block/oak_log_top", "side": "minecraft:block/oak_log"}}
                """));
        return (namespace, path) -> documents.get(namespace + ":" + path);
    }

    @Test
    void verticalAxisResolvesEndAndSideFacesCorrectly() {
        Map<Face, String> faces = BlockModelResolver
                .resolve("minecraft", "oak_log", Map.of("axis", "y"), logFixture())
                .orElseThrow();
        assertEquals("minecraft:block/oak_log_top", faces.get(Face.UP));
        assertEquals("minecraft:block/oak_log_top", faces.get(Face.DOWN));
        assertEquals("minecraft:block/oak_log", faces.get(Face.NORTH));
        assertEquals("minecraft:block/oak_log", faces.get(Face.EAST));
    }

    @Test
    void horizontalAxisReturnsNoEvidenceRatherThanTheWrongFaces() {
        // axis=z rotates the model 90 degrees about X: naively reusing the vertical face mapping
        // would read the log's end-grain as its side and vice versa. Refusing is correct here.
        Optional<Map<Face, String>> faces =
                BlockModelResolver.resolve("minecraft", "oak_log", Map.of("axis", "z"), logFixture());
        assertFalse(faces.isPresent());

        Optional<Map<Face, String>> faces2 =
                BlockModelResolver.resolve("minecraft", "oak_log", Map.of("axis", "x"), logFixture());
        assertFalse(faces2.isPresent());
    }

    @Test
    void unmatchedStateValueReturnsNoEvidence() {
        Optional<Map<Face, String>> faces =
                BlockModelResolver.resolve("minecraft", "oak_log", Map.of("axis", "diagonal"), logFixture());
        assertFalse(faces.isPresent());
    }

    @Test
    void missingBlockstateReturnsNoEvidence() {
        Optional<Map<Face, String>> faces =
                BlockModelResolver.resolve("minecraft", "nonexistent_block", Map.of(), logFixture());
        assertFalse(faces.isPresent());
    }

    @Test
    void cacheKeyDiffersForDifferentStateValuesOfTheSameBlock() {
        String vertical = BlockModelResolver.cacheKey("minecraft:oak_log", Map.of("axis", "y"));
        String horizontalZ = BlockModelResolver.cacheKey("minecraft:oak_log", Map.of("axis", "z"));
        String horizontalX = BlockModelResolver.cacheKey("minecraft:oak_log", Map.of("axis", "x"));
        assertNotEquals(vertical, horizontalZ);
        assertNotEquals(vertical, horizontalX);
        assertNotEquals(horizontalZ, horizontalX);
    }

    @Test
    void cacheKeyIsStableForTheSameStateValuesRegardlessOfMapOrdering() {
        Map<String, String> a = new HashMap<>();
        a.put("axis", "y");
        a.put("waterlogged", "false");
        Map<String, String> b = new HashMap<>();
        b.put("waterlogged", "false");
        b.put("axis", "y");
        assertEquals(BlockModelResolver.cacheKey("minecraft:oak_log", a), BlockModelResolver.cacheKey("minecraft:oak_log", b));
    }

    @Test
    void cacheKeyDiffersForDifferentBlocksWithTheSameStateValues() {
        String oak = BlockModelResolver.cacheKey("minecraft:oak_log", Map.of("axis", "y"));
        String spruce = BlockModelResolver.cacheKey("minecraft:spruce_log", Map.of("axis", "y"));
        assertTrue(!oak.equals(spruce));
    }
}
