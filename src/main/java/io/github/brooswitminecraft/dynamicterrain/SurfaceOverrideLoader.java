package io.github.brooswitminecraft.dynamicterrain;

import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * Loads {@code data/<namespace>/dynamicterrain/surface_overrides/<name>.json}: an
 * object with optional {@code "blocks"} (block id -&gt; properties) and
 * {@code "families"} (a {@link SurfaceMath.Family} name, case-insensitive, -&gt;
 * properties) maps. Each properties object gives {@code grip}, {@code roughness},
 * {@code rollingResistance} and {@code deformability} (0..1; missing fields default
 * to 0). Files from every namespace merge, so other mods can declare their blocks'
 * surfaces with a datapack file and no code. Later files in id order win.
 */
public class SurfaceOverrideLoader extends SimpleJsonResourceReloadListener {
    public SurfaceOverrideLoader() {
        super(new Gson(), "dynamicterrain/surface_overrides");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        SurfaceOverrideTable loaded = new SurfaceOverrideTable();
        for (Map.Entry<ResourceLocation, JsonElement> file : new TreeMap<>(files).entrySet()) {
            JsonObject object = GsonHelper.convertToJsonObject(file.getValue(), file.getKey().toString());
            if (object.has("blocks")) {
                JsonObject blocks = GsonHelper.convertToJsonObject(object.get("blocks"), "blocks");
                for (Map.Entry<String, JsonElement> entry : blocks.entrySet()) {
                    loaded.putBlock(entry.getKey(), readProperties(GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey())));
                }
            }
            if (object.has("families")) {
                JsonObject families = GsonHelper.convertToJsonObject(object.get("families"), "families");
                for (Map.Entry<String, JsonElement> entry : families.entrySet()) {
                    loaded.putFamily(entry.getKey(), readProperties(GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey())));
                }
            }
        }
        SurfaceOverrides.setTable(loaded);
        DynamicTerrainMod.LOGGER.info("Loaded {} surface overrides", loaded.size());
    }

    private static SurfaceProperties readProperties(JsonObject object) {
        return new SurfaceProperties(
                GsonHelper.getAsDouble(object, "grip", 0.0),
                GsonHelper.getAsDouble(object, "roughness", 0.0),
                GsonHelper.getAsDouble(object, "rollingResistance", 0.0),
                GsonHelper.getAsDouble(object, "deformability", 0.0));
    }
}
