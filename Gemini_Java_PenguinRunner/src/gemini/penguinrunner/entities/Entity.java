package gemini.penguinrunner.entities;

import gemini.penguinrunner.world.Direction;

/**
 * Base class for all dynamic world entities.
 * Decoupled from rendering graphics contexts.
 */
public abstract class Entity {
    protected int row;
    protected int col;
    protected final int startRow;
    protected final int startCol;
    protected Direction lastDirection = Direction.RIGHT;
    protected boolean dead = false;

    public Entity(int row, int col) {
        this.row = row;
        this.col = col;
        this.startRow = row;
        this.startCol = col;
    }

    public int row() { return row; }
    public int col() { return col; }
    public int startRow() { return startRow; }
    public int startCol() { return startCol; }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void resetToStart() {
        this.row = startRow;
        this.col = startCol;
        this.dead = false;
    }

    public Direction lastDirection() { return lastDirection; }
    public void setLastDirection(Direction dir) {
        if (dir != null) this.lastDirection = dir;
    }

    public boolean isDead() { return dead; }
    public void setDead(boolean dead) { this.dead = dead; }
}

