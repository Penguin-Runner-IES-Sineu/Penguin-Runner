namespace Gemini.PenguinRunner.Entities;

using Gemini.PenguinRunner.AI;
using Gemini.PenguinRunner.Core;

public class IceCreamEnemy : Enemy
{
    public IceCreamEnemy(int row, int col, int respawnRow, int respawnCol)
        : base(row, col, respawnRow, respawnCol) { }

    public override Direction? DecideMove(World world, Pathfinder pathfinder)
    {
        var blocks = world.Blocks;
        int rows = blocks.GetLength(0);
        int cols = blocks.GetLength(1);

        int closestDist = int.MaxValue;
        int targetRow = -1;
        int targetCol = -1;

        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                var b = blocks[r, c];
                if (b != null && b.Type == TileType.IceCream)
                {
                    int dist = Math.Abs(r - Row) + Math.Abs(c - Col);
                    if (dist < closestDist)
                    {
                        closestDist = dist;
                        targetRow = r;
                        targetCol = c;
                    }
                }
            }
        }

        if (targetRow != -1)
        {
            var d = pathfinder.FindShortestDirection(blocks, Row, Col, targetRow, targetCol);
            if (d.HasValue) return d;
        }

        var player = world.Player;
        if (player == null || !player.IsAlive) return null;
        return pathfinder.FindShortestDirection(blocks, Row, Col, player.Row, player.Col);
    }

    public override string SpriteKey => "icecream_enemy";
}
