package gemini.penguinrunner.ai;

import gemini.penguinrunner.world.Direction;
import gemini.penguinrunner.world.Block;
import gemini.penguinrunner.world.TileType;

/**
 * High-performance, zero-allocation Breadth-First Search pathfinder for enemies.
 * Uses an incremental search token to avoid re-zeroing visited buffers,
 * and pre-allocated primitive arrays for parent pointers and the BFS queue.
 */
public class Pathfinder {
    private static final int MAX_CELLS = 1024; // Standard map is 32x22 = 704
    private final int[] queue = new int[MAX_CELLS];
    private final int[] visitedToken = new int[MAX_CELLS];
    private final int[] parent = new int[MAX_CELLS];
    private int currentToken = 1;

    public Direction findShortestDirection(Block[][] blocks, int startRow, int startCol, int targetRow, int targetCol) {
        if (blocks == null || blocks.length == 0 || blocks[0].length == 0) return null;
        int rows = blocks.length;
        int cols = blocks[0].length;
        int totalCells = rows * cols;

        if (startRow < 0 || startRow >= rows || startCol < 0 || startCol >= cols) return null;
        if (targetRow < 0 || targetRow >= rows || targetCol < 0 || targetCol >= cols) return null;
        if (startRow == targetRow && startCol == targetCol) return null;

        currentToken++;
        if (currentToken == Integer.MAX_VALUE) {
            java.util.Arrays.fill(visitedToken, 0);
            currentToken = 1;
        }

        int head = 0;
        int tail = 0;

        int startIdx = startRow * cols + startCol;
        int targetIdx = targetRow * cols + targetCol;

        queue[tail++] = startIdx;
        visitedToken[startIdx] = currentToken;
        parent[startIdx] = -1;

        boolean found = false;
        int bestIdx = startIdx;
        int minDistance = Math.abs(startRow - targetRow) + Math.abs(startCol - targetCol);

        while (head < tail) {
            int curr = queue[head++];
            int r = curr / cols;
            int c = curr % cols;

            if (curr == targetIdx) {
                found = true;
                bestIdx = curr;
                break;
            }

            int dist = Math.abs(r - targetRow) + Math.abs(c - targetCol);
            if (dist < minDistance) {
                minDistance = dist;
                bestIdx = curr;
            }

            // Expand 4 directions: UP, DOWN, LEFT, RIGHT
            for (Direction dir : Direction.values()) {
                int nr = r + dir.dRow();
                int nc = c + dir.dCol();
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;

                int nIdx = nr * cols + nc;
                if (visitedToken[nIdx] == currentToken) continue;

                if (!canTraverse(blocks, r, c, nr, nc, dir)) continue;

                visitedToken[nIdx] = currentToken;
                parent[nIdx] = curr;
                queue[tail++] = nIdx;
            }
        }

        int destination = found ? targetIdx : bestIdx;
        if (destination == startIdx) return null;

        // Trace parent pointers back to find the first step from start
        int curr = destination;
        while (parent[curr] != -1 && parent[curr] != startIdx) {
            curr = parent[curr];
        }

        if (parent[curr] != startIdx) return null;

        int stepRow = curr / cols;
        int stepCol = curr % cols;

        for (Direction dir : Direction.values()) {
            if (startRow + dir.dRow() == stepRow && startCol + dir.dCol() == stepCol) {
                return dir;
            }
        }

        return null;
    }

    private boolean canTraverse(Block[][] blocks, int r, int c, int nr, int nc, Direction dir) {
        Block target = blocks[nr][nc];
        if (target == null || target.isSolid()) return false;

        Block current = blocks[r][c];

        // Ladder mechanics
        boolean currentIsStair = (current != null && current.type() == TileType.STAIR);
        boolean targetIsStair = (target.type() == TileType.STAIR);
        boolean currentIsRail = (current != null && current.type() == TileType.RAIL);

        Block below = (r + 1 < blocks.length) ? blocks[r + 1][c] : null;
        boolean onGround = (below != null && (below.isSolid() || below.type() == TileType.STAIR));

        switch (dir) {
            case UP -> {
                return currentIsStair;
            }
            case DOWN -> {
                return targetIsStair || !onGround || currentIsStair;
            }
            case LEFT, RIGHT -> {
                return onGround || currentIsStair || currentIsRail;
            }
        }
        return true;
    }
}

