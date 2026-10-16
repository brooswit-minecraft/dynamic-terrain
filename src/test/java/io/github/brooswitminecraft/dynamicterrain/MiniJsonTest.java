package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class MiniJsonTest {
    @Test
    void parsesNestedObjectsArraysAndPrimitives() {
        Object parsed = MiniJson.parse("""
                {"a": "text", "b": 90, "c": -1.5, "d": true, "e": false, "f": null,
                 "g": {"inner": "value"}, "h": ["x", "y"]}
                """);
        Map<?, ?> root = (Map<?, ?>) parsed;
        assertEquals("text", root.get("a"));
        assertEquals(90.0, root.get("b"));
        assertEquals(-1.5, root.get("c"));
        assertEquals(Boolean.TRUE, root.get("d"));
        assertEquals(Boolean.FALSE, root.get("e"));
        assertNull(root.get("f"));
        assertEquals("value", ((Map<?, ?>) root.get("g")).get("inner"));
        assertEquals(List.of("x", "y"), root.get("h"));
    }

    @Test
    void parsesEscapedCharactersInStrings() {
        Object parsed = MiniJson.parse("\"a\\\"b\\\\c\"");
        assertEquals("a\"b\\c", parsed);
    }

    @Test
    void parsesEmptyObjectsAndArrays() {
        assertTrue(((Map<?, ?>) MiniJson.parse("{}")).isEmpty());
        assertTrue(((List<?>) MiniJson.parse("[]")).isEmpty());
    }
}
