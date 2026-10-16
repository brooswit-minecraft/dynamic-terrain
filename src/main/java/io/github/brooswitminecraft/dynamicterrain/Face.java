package io.github.brooswitminecraft.dynamicterrain;

/**
 * The six block faces, independent of any Minecraft type so {@link FaceUv}
 * and {@link TireContactSampler} stay plain Java, testable without the game.
 * Glue code (e.g. {@link Surfaces}) maps {@code net.minecraft.core.Direction}
 * to and from this.
 */
public enum Face {
    UP, DOWN, NORTH, SOUTH, EAST, WEST
}
