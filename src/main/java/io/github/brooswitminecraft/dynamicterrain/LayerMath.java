package io.github.brooswitminecraft.dynamicterrain;

/**
 * Pure layer arithmetic, free of Minecraft types so it can be unit tested
 * without bootstrapping the game. Heights are in sixteenths of a block.
 */
public final class LayerMath {
    public static final int MIN_LAYERS = 1;
    public static final int MAX_LAYERS = 16;

    private LayerMath() {}

    public static boolean canAddLayer(int layers) {
        return layers >= MIN_LAYERS && layers < MAX_LAYERS;
    }

    /** One more layer, clamped to a full block. */
    public static int addLayer(int layers) {
        return Math.min(MAX_LAYERS, Math.max(MIN_LAYERS, layers + 1));
    }

    /** Bottom of the occupied span, in sixteenths. Floor layers grow up from 0, ceiling layers hang from 16. */
    public static int minY(boolean ceiling, int layers) {
        return ceiling ? MAX_LAYERS - layers : 0;
    }

    /** Top of the occupied span, in sixteenths. */
    public static int maxY(boolean ceiling, int layers) {
        return ceiling ? MAX_LAYERS : layers;
    }
}
