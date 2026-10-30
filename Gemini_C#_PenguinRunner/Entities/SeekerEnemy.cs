namespace Gemini.PenguinRunner.Entities;

using Gemini.PenguinRunner.AI;
using Gemini.PenguinRunner.Core;

public class SeekerEnemy : Enemy
{
    public SeekerEnemy(int row, int col, int respawnRow, int respawnCol)
        : base(row, col, respawnRow, respawnCol) { }

    public override Direction? DecideMove(World world, Pathfinder pathfinder)
    {
        var player = world.Player;
        if (player == null || !player.IsAlive) return null;
        return pathfinder.FindShortestDirection(world.Blocks, Row, Col, player.Row, player.Col);
    }

    public override string SpriteKey => "seekerenemy";
}
