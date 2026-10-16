package io.github.brooswitminecraft.dynamicterrain;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Resolves which canonical texture backs a block face, by reading the
 * block's own blockstate/model JSON straight off the classpath (never the
 * active resource pack - see {@link CanonicalTextures}). Best-effort: a
 * block whose model this cannot resolve (multipart blockstates, a texture
 * variable this never saw, assets absent on a bare dedicated server) simply
 * yields no evidence for that face rather than guessing one.
 */
public final class BlockFaceTextures {
    private static final int MAX_MODEL_DEPTH = 16;
    private static final Map<ResourceLocation, Optional<Map<Direction, ResourceLocation>>> CACHE = new ConcurrentHashMap<>();

    private BlockFaceTextures() {}

    public static Optional<ResourceLocation> textureFor(BlockState state, Direction face) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return CACHE.computeIfAbsent(blockId, BlockFaceTextures::resolve).map(faces -> faces.get(face));
    }

    public static void invalidateAll() {
        CACHE.clear();
    }

    private static Optional<Map<Direction, ResourceLocation>> resolve(ResourceLocation blockId) {
        try {
            JsonObject blockstate = readJson(
                    "assets/" + blockId.getNamespace() + "/blockstates/" + blockId.getPath() + ".json");
            if (blockstate == null) {
                return Optional.empty();
            }
            String modelPath = firstVariantModel(blockstate);
            if (modelPath == null) {
                return Optional.empty();
            }

            Map<String, String> textureVars = new HashMap<>();
            JsonArray elements = resolveModel(ResourceLocation.parse(modelPath), textureVars, 0);
            if (elements == null) {
                return Optional.empty();
            }

            Map<Direction, ResourceLocation> faces = new HashMap<>();
            for (JsonElement elementJson : elements) {
                JsonObject faceObj = elementJson.getAsJsonObject().getAsJsonObject("faces");
                if (faceObj == null) {
                    continue;
                }
                for (Direction direction : Direction.values()) {
                    if (faces.containsKey(direction)) {
                        continue;
                    }
                    JsonObject face = faceObj.getAsJsonObject(direction.getSerializedName());
                    if (face == null || !face.has("texture")) {
                        continue;
                    }
                    String resolved = resolveVariable(face.get("texture").getAsString(), textureVars, 0);
                    if (resolved != null) {
                        faces.put(direction, toTextureLocation(resolved));
                    }
                }
            }
            return faces.isEmpty() ? Optional.empty() : Optional.of(faces);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** Picks the first variant's model: a best-effort default, not full blockstate-property matching. */
    private static String firstVariantModel(JsonObject blockstate) {
        JsonObject variants = blockstate.getAsJsonObject("variants");
        if (variants == null || variants.entrySet().isEmpty()) {
            return null;
        }
        JsonElement first = variants.entrySet().iterator().next().getValue();
        JsonObject variant = first.isJsonArray() ? first.getAsJsonArray().get(0).getAsJsonObject() : first.getAsJsonObject();
        return variant.has("model") ? variant.get("model").getAsString() : null;
    }

    /** Walks the model's parent chain; returns the nearest-to-leaf "elements" and merges "textures" up the chain. */
    private static JsonArray resolveModel(ResourceLocation modelId, Map<String, String> textureVars, int depth) {
        if (depth > MAX_MODEL_DEPTH) {
            return null;
        }
        JsonObject model = readJson("assets/" + modelId.getNamespace() + "/models/" + modelId.getPath() + ".json");
        if (model == null) {
            return null;
        }
        JsonObject textures = model.getAsJsonObject("textures");
        if (textures != null) {
            for (Map.Entry<String, JsonElement> entry : textures.entrySet()) {
                textureVars.putIfAbsent(entry.getKey(), entry.getValue().getAsString());
            }
        }
        if (model.has("elements")) {
            return model.getAsJsonArray("elements");
        }
        if (model.has("parent")) {
            return resolveModel(ResourceLocation.parse(model.get("parent").getAsString()), textureVars, depth + 1);
        }
        return null;
    }

    /** Resolves a "#variable" reference through the merged texture map until it reaches a literal id. */
    private static String resolveVariable(String value, Map<String, String> textureVars, int depth) {
        if (depth > MAX_MODEL_DEPTH || value == null) {
            return null;
        }
        if (!value.startsWith("#")) {
            return value;
        }
        return resolveVariable(textureVars.get(value.substring(1)), textureVars, depth + 1);
    }

    private static ResourceLocation toTextureLocation(String id) {
        return ResourceLocation.parse(id);
    }

    private static JsonObject readJson(String classpathPath) {
        try (InputStream in = BlockFaceTextures.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (in == null) {
                return null;
            }
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
