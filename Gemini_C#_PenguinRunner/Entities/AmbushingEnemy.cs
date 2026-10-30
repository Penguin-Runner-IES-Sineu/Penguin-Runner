namespace Gemini.PenguinRunner.Entities;

using Gemini.PenguinRunner.AI;
using Gemini.PenguinRunner.Core;

public class AmbushingEnemy : Enemy
{
    public AmbushingEnemy(int row, int col, int respawnRow, int respawnCol)
        : base(row, col, respawnRow, respawnCol) { }

    public override Direction? DecideMove(World world, Pathfinder pathfinder)
    {
        var player = world.Player;
        if (player == null || !player.IsAlive) return null;

        var pDir = player.LastDirection;
        int targetRow = player.Row + pDir.DRow() * 3;
        int targetCol = player.Col + pDir.DCol() * 3;

        if (!world.IsInBounds(targetRow, targetCol))
        {
            targetRow = player.Row;
            targetCol = player.Col;
        }

        var dir = pathfinder.FindShortestDirection(world.Blocks, Row, Col, targetRow, targetCol);
        return dir ?? pathfinder.FindShortestDirection(world.Blocks, Row, Col, player.Row, player.Col);
    }

    public override string SpriteKey => "ambushingenemy";
}
