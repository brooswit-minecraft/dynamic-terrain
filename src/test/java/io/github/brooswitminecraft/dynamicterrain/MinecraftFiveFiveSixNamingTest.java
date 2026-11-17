package io.github.brooswitminecraft.dynamicterrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * MINECRAFT-556: repository/display-name normalization must never touch the
 * shipped runtime mod ID. These two facts are asserted independently so a
 * future edit to either file trips a test instead of silently drifting.
 */
class MinecraftFiveFiveSixNamingTest {
    @Test
    void shippedModIdIsFrozen() {
        assertEquals("dynamicterrain", DynamicTerrainMod.MODID);
    }

    @Test
    void gradlePropertiesDisplayNameIsTheNormalizedName() throws IOException {
        Properties props = new Properties();
        try (var in = Files.newInputStream(Path.of("gradle.properties"))) {
            props.load(in);
        }
        assertEquals("dynamicterrain", props.getProperty("mod_id"));
        assertEquals("Legacy Dynamic Terrain", props.getProperty("mod_name"));
    }

    @Test
    void settingsGradleRootProjectNameIsTheNormalizedName() throws IOException {
        String settings = Files.readString(Path.of("settings.gradle"));
        assertTrue(settings.contains("rootProject.name = \"legacy-dynamic-terrain\""),
                "settings.gradle should set rootProject.name to legacy-dynamic-terrain");
    }
}
