package io.github.brooswitminecraft.dynamicterrain;

/** Horizontal heading, free of Minecraft types so the choice can be unit tested. */
public enum Heading {
    NORTH, SOUTH, EAST, WEST;

    /**
     * The heading from a block toward a player, given the player-minus-block
     * offset. The dominant axis wins; ties go to Z.
     */
    public static Heading toward(double dx, double dz) {
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? EAST : WEST;
        }
        return dz > 0 ? SOUTH : NORTH;
    }
}
