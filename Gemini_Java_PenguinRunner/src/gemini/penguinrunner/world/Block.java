package gemini.penguinrunner.world;

/**
 * High-performance, lightweight Block representation with bitmask autotiling cache.
 */
public class Block {
    private TileType type;
    private final TileType originalType;
    private final int row;
    private final int col;

    private boolean doorOpen = false;
    private boolean buttonPressed = false;
    private boolean burning = false;
    private int regenTurns = 0; // If broken ice, regenerates when countdown reaches 0
    private String autotileKey = null;

    public Block(TileType type, int row, int col) {
        this.type = type;
        this.originalType = type;
        this.row = row;
        this.col = col;
    }

    public TileType type() { return type; }
    public TileType originalType() { return originalType; }
    public int row() { return row; }
    public int col() { return col; }

    public void setType(TileType type) {
        this.type = type;
        this.autotileKey = null;
    }

    public boolean isSolid() {
        if (type == TileType.DOOR) return false;
        if (type == TileType.TRAPDOOR) return buttonPressed;
        return type.isSolid();
    }

    public boolean isBreakable() { return type.isBreakable(); }
    public boolean isPushable() { return type.isPushable(); }
    public boolean isClimbable() { return type.isClimbable(); }
    public boolean isRail() { return type.isRail(); }
    public boolean isCollectible() { return type.isCollectible(); }
    public boolean isDoor() { return originalType == TileType.DOOR; }
    public boolean isTrapdoor() { return originalType == TileType.TRAPDOOR; }
    public boolean isButton() { return originalType == TileType.BUTTON; }
    public boolean isBurning() { return burning; }

    public void setBurning(boolean burning) { this.burning = burning; }
    public boolean isDoorOpen() { return doorOpen; }
    public void setDoorOpen(boolean open) { this.doorOpen = open; }

    public boolean isButtonPressed() { return buttonPressed; }
    public void setButtonPressed(boolean pressed) {
        this.buttonPressed = pressed;
        if (originalType == TileType.TRAPDOOR) {
            this.type = pressed ? TileType.TRAPDOOR : TileType.BLANK;
        }
    }

    public int regenTurns() { return regenTurns; }
    public void setRegenTurns(int turns) { this.regenTurns = turns; }
    public void decrementRegen() {
        if (regenTurns > 0) {
            regenTurns--;
            if (regenTurns == 0 && type == TileType.MOLTEN) {
                setType(TileType.ICE);
            }
        }
    }

    public String autotileKey() { return autotileKey; }
    public void setAutotileKey(String key) { this.autotileKey = key; }
}

