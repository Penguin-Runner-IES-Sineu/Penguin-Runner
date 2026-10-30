package gemini.penguinrunner.entities;

import gemini.penguinrunner.ai.Pathfinder;
import gemini.penguinrunner.world.Direction;
import gemini.penguinrunner.world.World;

public abstract class Enemy extends Entity {
    protected final int respawnRow;
    protected final int respawnCol;
    protected int reviveCountdown = 0;

    public Enemy(int row, int col, int respawnRow, int respawnCol) {
        super(row, col);
        this.respawnRow = respawnRow;
        this.respawnCol = respawnCol;
    }

    public int respawnRow() { return respawnRow; }
    public int respawnCol() { return respawnCol; }
    public int reviveCountdown() { return reviveCountdown; }

    public void kill(int turnsToRevive) {
        this.dead = true;
        this.reviveCountdown = turnsToRevive;
    }

    public void tickRevive() {
        if (dead && reviveCountdown > 0) {
            reviveCountdown--;
            if (reviveCountdown <= 0) {
                revive();
            }
        }
    }

    public void revive() {
        this.dead = false;
        this.row = respawnRow;
        this.col = respawnCol;
    }

    public abstract Direction decideMove(World world, Pathfinder pathfinder);
    public abstract String spriteKey();
}

