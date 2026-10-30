namespace Gemini.PenguinRunner.Items;

using Gemini.PenguinRunner.Core;
using Gemini.PenguinRunner.Entities;

public class Flamethrower : IItem
{
    public int UsesLeft { get; private set; }

    public Flamethrower(int uses = 3)
    {
        UsesLeft = uses;
    }

    public string Name => "flamethrower";
    public bool IsExpired => UsesLeft <= 0;

    public bool Use(Player player, World world)
    {
        if (UsesLeft <= 0) return false;
        UsesLeft--;

        var dir = player.LastDirection;
        int r = player.Row;
        int c = player.Col;

        for (int step = 1; step <= 3; step++)
        {
            int tr = r + dir.DRow() * step;
            int tc = c + dir.DCol() * step;
            if (!world.IsInBounds(tr, tc)) break;

            var b = world.GetBlock(tr, tc);
            if (b == null) continue;

            if (b.Type == TileType.Wood)
            {
                b.Type = TileType.Burnt;
                b.Burning = true;
            }
            else if (b.Type is TileType.Ice or TileType.Molten)
            {
                b.Type = TileType.Molten;
                b.RegenTurns = 8;
            }
            else if (b.IsSolid)
            {
                break;
            }
        }

        world.Audio.PlaySound("flame");
        return true;
    }
}
