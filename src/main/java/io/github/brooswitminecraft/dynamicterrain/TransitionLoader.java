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
 * Loads {@code data/<namespace>/dynamicterrain/transitions/<name>.json}. The
 * file name is the transition name, the content maps a block id to the block
 * it becomes: {@code {"minecraft:cobblestone": "minecraft:mossy_cobblestone"}}.
 * Files from every namespace for the same name merge, so other mods can opt
 * their blocks in with a datapack file and no code. Later files in id order win.
 */
public class TransitionLoader extends SimpleJsonResourceReloadListener {
    public TransitionLoader() {
        super(new Gson(), "dynamicterrain/transitions");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        TransitionTable loaded = new TransitionTable();
        for (Map.Entry<ResourceLocation, JsonElement> file : new TreeMap<>(files).entrySet()) {
            String transition = file.getKey().getPath();
            JsonObject object = GsonHelper.convertToJsonObject(file.getValue(), file.getKey().toString());
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                loaded.put(transition, entry.getKey(), GsonHelper.convertToString(entry.getValue(), entry.getKey()));
            }
        }
        Transitions.setTable(loaded);
        DynamicTerrainMod.LOGGER.info("Loaded {} block transitions", loaded.size());
    }
}
