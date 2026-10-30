namespace Gemini.PenguinRunner.Core;

public enum TileType
{
    Blank,
    Empty,
    Ice,
    Wall,
    Stair,
    Rail,
    Stone,
    Wood,
    Door,
    Trapdoor,
    Button,
    IceCream,
    Flamethrower,
    Teleport,
    Molten,
    Burnt
}

public static class TileTypeExtensions
{
    public static bool IsSolid(this TileType type) => type switch
    {
        TileType.Ice or TileType.Wall or TileType.Stone or TileType.Wood => true,
        _ => false
    };

    public static bool IsBreakable(this TileType type) => type == TileType.Ice;
    public static bool IsClimbable(this TileType type) => type == TileType.Stair;
    public static bool IsRail(this TileType type) => type == TileType.Rail;
    public static bool IsPushable(this TileType type) => type is TileType.Stone or TileType.Wood;
    public static bool IsBurnable(this TileType type) => type == TileType.Wood;
    public static bool IsCollectible(this TileType type) => type is TileType.IceCream or TileType.Flamethrower or TileType.Teleport;
}
