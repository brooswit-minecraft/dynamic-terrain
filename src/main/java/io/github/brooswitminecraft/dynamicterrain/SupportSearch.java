package io.github.brooswitminecraft.dynamicterrain;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * The structural support search, free of Minecraft types so it can be unit
 * tested. It is an intentionally approximate weighted flood-fill through
 * connected solid material: every solid cell reachable from the start adds
 * support by how it connects (a cell below is worth 3, beside 2, above 1), and
 * the search stops as soon as the threshold is met. Because nothing requires
 * the support to reach the ground, a sufficiently large floating mass holds
 * itself up. Each cell is counted once, however many paths reach it, and the
 * visited count is capped so a search can never run away.
 */
public final class SupportSearch {
    public static final int DOWN_SUPPORT = 3;
    public static final int SIDE_SUPPORT = 2;
    public static final int UP_SUPPORT = 1;

    /** What a world cell is, as far as support is concerned. */
    public enum Cell {
        /** Air, fluid or anything non-solid. Carries no support. */
        EMPTY,
        /** Solid material that counts toward support. */
        SOLID,
        /** Bedrock-like or world-bottom material that fully anchors whatever touches it. */
        ANCHOR
    }

    /** Read-only view of the world around the search. */
    @FunctionalInterface
    public interface Grid {
        Cell at(int x, int y, int z);
    }

    private static final int[][] STEPS = {
            {0, -1, 0, DOWN_SUPPORT},
            {1, 0, 0, SIDE_SUPPORT}, {-1, 0, 0, SIDE_SUPPORT}, {0, 0, 1, SIDE_SUPPORT}, {0, 0, -1, SIDE_SUPPORT},
            {0, 1, 0, UP_SUPPORT},
    };

    private SupportSearch() {}

    /**
     * @param threshold support score the cell needs to stay up
     * @param maxVisited cap on cells examined; the search gives up (unsupported) past it
     * @return true if the connected material around (x,y,z) supplies at least {@code threshold}
     */
    public static boolean isSupported(Grid grid, int x, int y, int z, int threshold, int maxVisited) {
        if (threshold <= 0) {
            return true;
        }
        Set<Long> seen = new HashSet<>();
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        seen.add(key(x, y, z));
        queue.add(new int[] {x, y, z});
        int score = 0;
        int visited = 0;
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            for (int[] step : STEPS) {
                int nx = cell[0] + step[0];
                int ny = cell[1] + step[1];
                int nz = cell[2] + step[2];
                if (!seen.add(key(nx, ny, nz))) {
                    continue;
                }
                Cell neighbour = grid.at(nx, ny, nz);
                if (neighbour == Cell.ANCHOR) {
                    return true;
                }
                if (neighbour != Cell.SOLID) {
                    continue;
                }
                score += step[3];
                if (score >= threshold) {
                    return true;
                }
                if (++visited >= maxVisited) {
                    return false;
                }
                queue.add(new int[] {nx, ny, nz});
            }
        }
        return false;
    }

    private static long key(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }
}
