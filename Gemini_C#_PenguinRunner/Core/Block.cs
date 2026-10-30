namespace Gemini.PenguinRunner.Core;

public class Block
{
    public TileType Type { get; set; }
    public TileType OriginalType { get; }
    public int Row { get; }
    public int Col { get; }

    public bool DoorOpen { get; set; }
    public bool ButtonPressed { get; set; }
    public bool Burning { get; set; }
    public int RegenTurns { get; set; }
    public string? AutotileKey { get; set; }

    public Block(TileType type, int row, int col)
    {
        Type = type;
        OriginalType = type;
        Row = row;
        Col = col;
    }

    public bool IsSolid => Type switch
    {
        TileType.Door => false,
        TileType.Trapdoor => ButtonPressed,
        _ => Type.IsSolid()
    };

    public bool IsBreakable => Type.IsBreakable();
    public bool IsPushable => Type.IsPushable();
    public bool IsClimbable => Type.IsClimbable();
    public bool IsRail => Type.IsRail();
    public bool IsCollectible => Type.IsCollectible();
    public bool IsDoor => OriginalType == TileType.Door;
    public bool IsTrapdoor => OriginalType == TileType.Trapdoor;
    public bool IsButton => OriginalType == TileType.Button;

    public void DecrementRegen()
    {
        if (RegenTurns > 0)
        {
            RegenTurns--;
            if (RegenTurns == 0 && Type == TileType.Molten)
            {
                Type = TileType.Ice;
                AutotileKey = null;
            }
        }
    }
}
