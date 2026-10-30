package gemini.penguinrunner.world;

public enum TileType {
    BLANK('.', false, false, false, false, false, false, false),
    EMPTY(' ', false, false, false, false, false, false, false),
    ICE('#', true, true, false, false, false, false, false),
    WALL('=', true, false, false, false, false, false, false),
    STAIR('H', false, false, true, false, false, false, false),
    RAIL('-', false, false, false, true, false, false, false),
    STONE('S', true, false, false, false, true, false, false),
    WOOD('W', true, false, false, false, true, true, false),
    DOOR('D', false, false, false, false, false, false, false),
    TRAPDOOR('T', false, false, false, false, false, false, false),
    BUTTON('B', false, false, false, false, false, false, false),
    ICECREAM('G', false, false, false, false, false, false, true),
    FLAMETHROWER('F', false, false, false, false, false, false, true),
    TELEPORT('P', false, false, false, false, false, false, true),
    MOLTEN('m', false, false, false, false, false, false, false),
    BURNT('b', false, false, false, false, false, false, false);

    private final char ascii;
    private final boolean solid;
    private final boolean breakable;
    private final boolean climbable;
    private final boolean rail;
    private final boolean pushable;
    private final boolean burnable;
    private final boolean collectible;

    TileType(char ascii, boolean solid, boolean breakable, boolean climbable, boolean rail, boolean pushable, boolean burnable, boolean collectible) {
        this.ascii = ascii;
        this.solid = solid;
        this.breakable = breakable;
        this.climbable = climbable;
        this.rail = rail;
        this.pushable = pushable;
        this.burnable = burnable;
        this.collectible = collectible;
    }

    public char ascii() { return ascii; }
    public boolean isSolid() { return solid; }
    public boolean isBreakable() { return breakable; }
    public boolean isClimbable() { return climbable; }
    public boolean isRail() { return rail; }
    public boolean isPushable() { return pushable; }
    public boolean isBurnable() { return burnable; }
    public boolean isCollectible() { return collectible; }
    public boolean isDeadlyForEnemy() { return this == MOLTEN; }

    public static TileType fromAscii(char c) {
        return switch (c) {
            case '#' -> ICE;
            case '=' -> WALL;
            case 'H' -> STAIR;
            case '-' -> RAIL;
            case 'S' -> STONE;
            case 'W' -> WOOD;
            case 'D' -> DOOR;
            case 'T' -> TRAPDOOR;
            case 'B' -> BUTTON;
            case 'G' -> ICECREAM;
            case 'F' -> FLAMETHROWER;
            case 'P' -> TELEPORT;
            case 'm', 'M' -> MOLTEN;
            case 'b' -> BURNT;
            case ' ' -> EMPTY;
            default -> BLANK;
        };
    }
}

