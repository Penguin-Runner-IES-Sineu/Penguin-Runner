package gemini.penguinrunner.entities;

import gemini.penguinrunner.ai.Pathfinder;
import gemini.penguinrunner.world.Block;
import gemini.penguinrunner.world.Direction;
import gemini.penguinrunner.world.TileType;
import gemini.penguinrunner.world.World;

public class IceCreamEnemy extends Enemy {
    public IceCreamEnemy(int row, int col, int respawnRow, int respawnCol) {
        super(row, col, respawnRow, respawnCol);
    }

    @Override
    public Direction decideMove(World world, Pathfinder pathfinder) {
        // Find closest ice cream
        Block[][] blocks = world.blocks();
        int closestDist = Integer.MAX_VALUE;
        int targetRow = -1;
        int targetCol = -1;

        for (int r = 0; r < blocks.length; r++) {
            for (int c = 0; c < blocks[r].length; c++) {
                Block b = blocks[r][c];
                if (b != null && b.type() == TileType.ICECREAM) {
                    int dist = Math.abs(r - row) + Math.abs(c - col);
                    if (dist < closestDist) {
                        closestDist = dist;
                        targetRow = r;
                        targetCol = c;
                    }
                }
            }
        }

        if (targetRow != -1) {
            Direction dir = pathfinder.findShortestDirection(blocks, row, col, targetRow, targetCol);
            if (dir != null) return dir;
        }

        // Fallback to player
        Player player = world.player();
        if (player == null || player.isDead()) return null;
        return pathfinder.findShortestDirection(blocks, row, col, player.row(), player.col());
    }

    @Override
    public String spriteKey() {
        return "icecream_enemy";
    }
}

