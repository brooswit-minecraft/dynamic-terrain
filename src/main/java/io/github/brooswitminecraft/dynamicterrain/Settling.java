package io.github.brooswitminecraft.dynamicterrain;

/**
 * The pure decision for whether a smooth() step settles grass as dirt. Free
 * of Minecraft types, mirroring {@link SmoothRules}: Smoothing supplies
 * whether each side's material is grass, and whether this move counts as
 * disturbance at all; this class decides which end(s) actually settle.
 */
public final class Settling {
    private Settling() {}

    /** Whether the source and/or destination settle to dirt for this step. */
    public record Result(boolean source, boolean destination) {}

    /**
     * @param disturb whether this move counts as disturbance (erosion) rather
     *     than manual grading or the debug command — the shared opt-in both
     *     erosion callers set and both non-erosion callers leave off
     * @param sourceIsGrass whether the material leaving the source is grass
     * @param kind what the destination cell is relative to the source
     * @param destinationIsGrass whether the material already occupying the
     *     destination (meaningful only for {@code SAME_MATERIAL}) is grass
     */
    public static Result settle(boolean disturb, boolean sourceIsGrass, SmoothRules.Destination kind, boolean destinationIsGrass) {
        if (!disturb) {
            return new Result(false, false);
        }
        boolean destinationSettles = switch (kind) {
            // Sediment deposited into empty space carries the mover's material.
            case EMPTY -> sourceIsGrass;
            // The destination keeps its own material; grass receiving soil still settles.
            case SAME_MATERIAL -> destinationIsGrass;
            case BLOCKED -> false;
        };
        return new Result(sourceIsGrass, destinationSettles);
    }
}
