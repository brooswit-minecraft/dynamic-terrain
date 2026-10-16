package io.github.brooswitminecraft.dynamicterrain;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Resolves which canonical texture backs each face of a block, for one specific block state, by
 * reading the block's own blockstate/model JSON off the classpath (never the active resource pack
 * - see {@link CanonicalTextures}) and matching the state's actual property values against the
 * blockstate's variant keys, the way vanilla does - not just taking whichever variant comes first.
 *
 * <p>Honest best-effort: a variant this can't safely resolve - no "variants" block (multipart), a
 * model-level x/y rotation, a face with its own UV rotation or crop, a texture variable it never
 * saw, a model that isn't a single unrotated full-cube element - comes back with that face (or
 * the whole block) simply absent, rather than a guessed mapping. The sampler this feeds assumes
 * the contacted face spans the full 0..1 of block-local space and the full 0..1 of the texture,
 * which only holds for a plain full-size cube: anything else (a slab's partial extent, a stair's
 * several elements, a cropped face UV) would silently misreport evidence for geometry that isn't
 * what the tire actually touched. A log placed on its side is the motivating case for the
 * rotation check: guessing would read its end-grain texture as if it still faced up, and a tire
 * would misread cross-grain wood as along-grain.
 */
final class BlockModelResolver {
    private static final int MAX_MODEL_DEPTH = 16;

    private BlockModelResolver() {}

    /** Loads one JSON document given a namespace and a path like {@code "models/block/oak_log"}. */
    interface JsonLoader {
        Object load(String namespace, String path);
    }

    static Optional<Map<Face, String>> resolve(String namespace, String blockPath, Map<String, String> stateValues) {
        return resolve(namespace, blockPath, stateValues, BlockModelResolver::readClasspathJson);
    }

    static Optional<Map<Face, String>> resolve(String namespace, String blockPath, Map<String, String> stateValues,
            JsonLoader loader) {
        Map<String, Object> blockstate = asObject(loader.load(namespace, "blockstates/" + blockPath));
        if (blockstate == null) {
            return Optional.empty();
        }
        Map<String, Object> variants = asObject(blockstate.get("variants"));
        if (variants == null) {
            return Optional.empty(); // no "variants" block (e.g. multipart): unsupported, honest no-evidence
        }
        Map<String, Object> matched = matchVariant(variants, stateValues);
        if (matched == null || nonZero(matched.get("x")) || nonZero(matched.get("y"))) {
            // No matching variant, or one that rotates the whole model: rotation changes which
            // world face each model face ends up on, which this does not simulate.
            return Optional.empty();
        }
        if (!(matched.get("model") instanceof String modelId)) {
            return Optional.empty();
        }
        String[] model = splitId(modelId, namespace);

        Map<String, String> textureVars = new HashMap<>();
        List<Object> elements = resolveElements(model[0], model[1], textureVars, loader, 0);
        // More than one element means more than one cuboid contributes to at least one face
        // direction (e.g. a stair): which one a tire actually touches depends on exactly where it
        // lands, which this doesn't model. Refuse rather than arbitrarily picking one.
        if (elements == null || elements.size() != 1) {
            return Optional.empty();
        }
        Map<String, Object> element = asObject(elements.get(0));
        Map<String, Object> faceMap = element == null ? null : asObject(element.get("faces"));
        if (faceMap == null || !isFullUnrotatedCube(element)) {
            return Optional.empty();
        }

        Map<Face, String> faces = new HashMap<>();
        for (Face face : Face.values()) {
            Map<String, Object> faceObj = asObject(faceMap.get(face.name().toLowerCase()));
            if (faceObj == null || nonZero(faceObj.get("rotation")) || !isDefaultOrAbsentUv(faceObj.get("uv"))
                    || !(faceObj.get("texture") instanceof String texture)) {
                // a per-face UV rotation or crop this doesn't account for: skip, don't guess
                continue;
            }
            String resolved = resolveVariable(texture, textureVars, 0);
            if (resolved != null) {
                faces.put(face, resolveId(resolved, namespace));
            }
        }
        return faces.isEmpty() ? Optional.empty() : Optional.of(faces);
    }

    /** A plain 0..16 cube with no element-level rotation: the only geometry the sampler's 0..1 assumption fits. */
    private static boolean isFullUnrotatedCube(Map<String, Object> element) {
        return !element.containsKey("rotation")
                && matchesExtent(element.get("from"), 0, 0, 0)
                && matchesExtent(element.get("to"), 16, 16, 16);
    }

    private static boolean matchesExtent(Object value, double x, double y, double z) {
        return value instanceof List<?> list && list.size() == 3
                && nearlyEquals(list.get(0), x) && nearlyEquals(list.get(1), y) && nearlyEquals(list.get(2), z);
    }

    /** Vanilla's implicit default UV for a full-cube face is the whole 16x16 texture; anything else crops it. */
    private static boolean isDefaultOrAbsentUv(Object uv) {
        if (uv == null) {
            return true;
        }
        return uv instanceof List<?> list && list.size() == 4 && nearlyEquals(list.get(0), 0) && nearlyEquals(list.get(1), 0)
                && nearlyEquals(list.get(2), 16) && nearlyEquals(list.get(3), 16);
    }

    private static boolean nearlyEquals(Object value, double expected) {
        return value instanceof Double d && Math.abs(d - expected) < 1e-6;
    }

    /**
     * A cache key that depends on every property value, so two states of the same block (a log's
     * vertical vs horizontal axis, say) never share a resolved face mapping.
     */
    static String cacheKey(String blockId, Map<String, String> stateValues) {
        StringBuilder key = new StringBuilder(blockId);
        for (Map.Entry<String, String> entry : new TreeMap<>(stateValues).entrySet()) {
            key.append(';').append(entry.getKey()).append('=').append(entry.getValue());
        }
        return key.toString();
    }

    private static Map<String, Object> matchVariant(Map<String, Object> variants, Map<String, String> stateValues) {
        for (Map.Entry<String, Object> entry : variants.entrySet()) {
            if (keyMatches(entry.getKey(), stateValues)) {
                Object value = entry.getValue();
                if (value instanceof List<?> list) {
                    return list.isEmpty() ? null : asObject(list.get(0));
                }
                return asObject(value);
            }
        }
        return null;
    }

    private static boolean keyMatches(String key, Map<String, String> stateValues) {
        if (key.isEmpty()) {
            return true;
        }
        for (String pair : key.split(",")) {
            int eq = pair.indexOf('=');
            if (eq < 0 || !pair.substring(eq + 1).equals(stateValues.get(pair.substring(0, eq)))) {
                return false;
            }
        }
        return true;
    }

    private static List<Object> resolveElements(String namespace, String modelPath, Map<String, String> textureVars,
            JsonLoader loader, int depth) {
        if (depth > MAX_MODEL_DEPTH) {
            return null;
        }
        Map<String, Object> model = asObject(loader.load(namespace, "models/" + modelPath));
        if (model == null) {
            return null;
        }
        Map<String, Object> textures = asObject(model.get("textures"));
        if (textures != null) {
            for (Map.Entry<String, Object> entry : textures.entrySet()) {
                if (entry.getValue() instanceof String value) {
                    textureVars.putIfAbsent(entry.getKey(), value);
                }
            }
        }
        if (model.get("elements") instanceof List<?> elements) {
            @SuppressWarnings("unchecked")
            List<Object> cast = (List<Object>) elements;
            return cast;
        }
        if (model.get("parent") instanceof String parent) {
            String[] parts = splitId(parent, namespace);
            return resolveElements(parts[0], parts[1], textureVars, loader, depth + 1);
        }
        return null;
    }

    private static String resolveVariable(String value, Map<String, String> textureVars, int depth) {
        if (depth > MAX_MODEL_DEPTH || value == null) {
            return null;
        }
        if (!value.startsWith("#")) {
            return value;
        }
        return resolveVariable(textureVars.get(value.substring(1)), textureVars, depth + 1);
    }

    private static boolean nonZero(Object value) {
        return value instanceof Double d && d != 0.0;
    }

    private static String[] splitId(String id, String defaultNamespace) {
        int colon = id.indexOf(':');
        return colon < 0 ? new String[] {defaultNamespace, id} : new String[] {id.substring(0, colon), id.substring(colon + 1)};
    }

    private static String resolveId(String id, String defaultNamespace) {
        String[] parts = splitId(id, defaultNamespace);
        return parts[0] + ":" + parts[1];
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asObject(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    private static Object readClasspathJson(String namespace, String path) {
        String classpathPath = "assets/" + namespace + "/" + path + ".json";
        try (InputStream in = BlockModelResolver.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (in == null) {
                return null;
            }
            return MiniJson.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
