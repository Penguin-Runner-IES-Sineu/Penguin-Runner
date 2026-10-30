package gemini.penguinrunner.entities;

import gemini.penguinrunner.ai.Pathfinder;
import gemini.penguinrunner.world.Direction;
import gemini.penguinrunner.world.World;

public class SeekerEnemy extends Enemy {
    public SeekerEnemy(int row, int col, int respawnRow, int respawnCol) {
        super(row, col, respawnRow, respawnCol);
    }

    @Override
    public Direction decideMove(World world, Pathfinder pathfinder) {
        Player player = world.player();
        if (player == null || player.isDead()) return null;
        return pathfinder.findShortestDirection(world.blocks(), row, col, player.row(), player.col());
    }

    @Override
    public String spriteKey() {
        return "seekerenemy";
    }
}

