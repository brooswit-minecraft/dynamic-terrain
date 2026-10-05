package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/** Guards the shipped damage chains: every chain must end, and the documented ones must hold. */
class DamageChainsTest {
    private static Map<String, String> load() throws IOException {
        try (InputStream in = DamageChainsTest.class.getResourceAsStream(
                "/data/dynamicterrain/dynamicterrain/transitions/damage.json")) {
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> map = new HashMap<>();
            Matcher m = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            while (m.find()) {
                map.put(m.group(1), m.group(2));
            }
            return map;
        }
    }

    private static String endOf(Map<String, String> chains, String start) {
        String current = start;
        for (int steps = 0; chains.containsKey(current); steps++) {
            assertTrue(steps < 20, "cycle in damage chains starting at " + start);
            current = chains.get(current);
        }
        return current;
    }

    @Test
    void everyChainEndsInAir() throws IOException {
        Map<String, String> chains = load();
        for (String start : chains.keySet()) {
            assertEquals("minecraft:air", endOf(chains, start), start);
        }
    }

    @Test
    void documentedChainsHold() throws IOException {
        Map<String, String> chains = load();
        assertEquals("minecraft:dirt", chains.get("minecraft:grass_block"));
        assertEquals("minecraft:gravel", chains.get("minecraft:dirt"));
        assertEquals("minecraft:cobblestone", chains.get("minecraft:smooth_stone"));
        assertEquals("minecraft:gravel", chains.get("minecraft:cobblestone"));
    }
}
