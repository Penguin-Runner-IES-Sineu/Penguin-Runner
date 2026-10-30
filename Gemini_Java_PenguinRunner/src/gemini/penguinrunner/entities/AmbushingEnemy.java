package gemini.penguinrunner.entities;

import gemini.penguinrunner.ai.Pathfinder;
import gemini.penguinrunner.world.Direction;
import gemini.penguinrunner.world.World;

public class AmbushingEnemy extends Enemy {
    public AmbushingEnemy(int row, int col, int respawnRow, int respawnCol) {
        super(row, col, respawnRow, respawnCol);
    }

    @Override
    public Direction decideMove(World world, Pathfinder pathfinder) {
        Player player = world.player();
        if (player == null || player.isDead()) return null;

        // Predict 3 tiles ahead in player's last direction
        Direction pDir = player.lastDirection();
        int targetRow = player.row() + pDir.dRow() * 3;
        int targetCol = player.col() + pDir.dCol() * 3;

        if (!world.isInBounds(targetRow, targetCol)) {
            targetRow = player.row();
            targetCol = player.col();
        }

        Direction dir = pathfinder.findShortestDirection(world.blocks(), row, col, targetRow, targetCol);
        if (dir == null) {
            // Fallback directly to player position
            dir = pathfinder.findShortestDirection(world.blocks(), row, col, player.row(), player.col());
        }
        return dir;
    }

    @Override
    public String spriteKey() {
        return "ambushingenemy";
    }
}

