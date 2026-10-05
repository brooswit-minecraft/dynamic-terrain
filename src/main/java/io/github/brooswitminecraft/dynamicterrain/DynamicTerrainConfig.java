package io.github.brooswitminecraft.dynamicterrain;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config. Terrain-altering systems default to OFF so existing worlds are safe. */
public final class DynamicTerrainConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue EROSION_ENABLED;
    public static final ModConfigSpec.IntValue WATER_SAMPLES_PER_SECOND;
    public static final ModConfigSpec.BooleanValue ENTITY_EROSION;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        EROSION_ENABLED = builder
                .comment("Let erode() change terrain. When false, erode() does nothing. Manual tools still work.")
                .define("erosionEnabled", false);
        WATER_SAMPLES_PER_SECOND = builder
                .comment("Random positions sampled around each player per second for water erosion. 0 turns water erosion off.")
                .defineInRange("waterSamplesPerSecond", 128, 0, 4096);
        ENTITY_EROSION = builder
                .comment("Let walking players and mobs wear the ground under them (mass x speed). Needs erosionEnabled.")
                .define("entityErosion", true);
        SPEC = builder.build();
    }

    private DynamicTerrainConfig() {}
}
