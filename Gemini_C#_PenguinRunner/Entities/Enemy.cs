namespace Gemini.PenguinRunner.Entities;

using Gemini.PenguinRunner.AI;
using Gemini.PenguinRunner.Core;

public abstract class Enemy : Entity
{
    public int RespawnRow { get; }
    public int RespawnCol { get; }
    public int ReviveCountdown { get; protected set; }

    protected Enemy(int row, int col, int respawnRow, int respawnCol) : base(row, col)
    {
        RespawnRow = respawnRow;
        RespawnCol = respawnCol;
    }

    public void Kill(int turnsToRevive)
    {
        IsDead = true;
        ReviveCountdown = turnsToRevive;
    }

    public void TickRevive()
    {
        if (IsDead && ReviveCountdown > 0)
        {
            ReviveCountdown--;
            if (ReviveCountdown <= 0)
            {
                Revive();
            }
        }
    }

    public void Revive()
    {
        IsDead = false;
        Row = RespawnRow;
        Col = RespawnCol;
    }

    public abstract Direction? DecideMove(World world, Pathfinder pathfinder);
    public abstract string SpriteKey { get; }
}
