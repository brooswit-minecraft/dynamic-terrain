package io.github.brooswitminecraft.dynamicterrain;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import net.minecraft.resources.ResourceLocation;

/**
 * Per-texel analysis of the canonical texture Minecraft or the owning mod
 * ships for a block, never the player's resource pack: a cosmetic pack must
 * not change physics. Reading straight off the classpath rather than through
 * Minecraft's resource-pack-aware {@code ResourceManager} is what gives that
 * guarantee: an active resource pack is a separate zip/folder that is never
 * on the classpath, so a classpath read always returns the asset the jar
 * shipped with. Results are cached per texture; call {@link #invalidate} or
 * {@link #invalidateAll} when the owning mod/data version changes.
 */
public final class CanonicalTextures {
    private static final Map<ResourceLocation, Optional<TexelField>> CACHE = new ConcurrentHashMap<>();

    private CanonicalTextures() {}

    /** @param textureId a model-style texture id, e.g. {@code minecraft:block/grass_block_top} */
    public static Optional<TexelField> at(ResourceLocation textureId) {
        return CACHE.computeIfAbsent(textureId, CanonicalTextures::load);
    }

    public static void invalidate(ResourceLocation textureId) {
        CACHE.remove(textureId);
    }

    public static void invalidateAll() {
        CACHE.clear();
    }

    private static Optional<TexelField> load(ResourceLocation textureId) {
        String path = "assets/" + textureId.getNamespace() + "/textures/" + textureId.getPath() + ".png";
        try (InputStream in = CanonicalTextures.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                return Optional.empty();
            }
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                return Optional.empty();
            }
            return Optional.of(TextureAnalyzer.analyze(image));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
