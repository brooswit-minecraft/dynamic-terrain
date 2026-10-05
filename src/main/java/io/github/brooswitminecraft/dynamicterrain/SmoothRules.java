package io.github.brooswitminecraft.dynamicterrain;

import java.util.Optional;

/**
 * The pure decision for one smooth() step, free of Minecraft types. Moves
 * exactly one layer (one sixteenth) from a source cell into a neighbour, or
 * rejects.
 */
public final class SmoothRules {
    private SmoothRules() {}

    /** A layered cell. A full block is 16 layers, floor-anchored. */
    public record Cell(int layers, boolean ceiling) {}

    /** Layers a slab is worth: a bottom slab is 8 floor layers, a top slab 8 ceiling layers, a double slab a full block. */
    public static Cell slab(boolean top, boolean doubleSlab) {
        if (doubleSlab) {
            return new Cell(LayerMath.MAX_LAYERS, false);
        }
        return new Cell(LayerMath.MAX_LAYERS / 2, top);
    }

    /** What is in the destination cell, relative to the source's material. */
    public enum Destination { EMPTY, SAME_MATERIAL, BLOCKED }

    /**
     * Result of an accepted step. {@code source} is null when the source lost
     * its last layer (it becomes air).
     */
    public record Plan(Cell source, Cell destination) {}

    /**
     * @param anchorSupported whether an empty destination could hold a layer
     *     anchored the same way as the source (a sturdy block below for floor
     *     layers, above for ceiling layers)
     */
    public static Optional<Plan> plan(Cell source, Destination kind, Cell destination, boolean anchorSupported) {
        Cell sourceAfter = source.layers() > LayerMath.MIN_LAYERS
                ? new Cell(source.layers() - 1, source.ceiling()) : null;
        switch (kind) {
            case EMPTY:
                if (!anchorSupported) {
                    return Optional.empty();
                }
                return Optional.of(new Plan(sourceAfter, new Cell(LayerMath.MIN_LAYERS, source.ceiling())));
            case SAME_MATERIAL:
                if (destination.ceiling() != source.ceiling() || !LayerMath.canAddLayer(destination.layers())) {
                    return Optional.empty();
                }
                return Optional.of(new Plan(sourceAfter, new Cell(LayerMath.addLayer(destination.layers()), destination.ceiling())));
            default:
                return Optional.empty();
        }
    }
}
