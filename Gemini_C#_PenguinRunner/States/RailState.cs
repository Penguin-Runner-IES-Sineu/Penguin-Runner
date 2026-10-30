namespace Gemini.PenguinRunner.States;

using Gemini.PenguinRunner.Core;

public class RailState : IPlayerState
{
    public void HandleInput(World world, Direction direction)
    {
        if (direction == Direction.Up) return;
        if (direction == Direction.Down)
        {
            world.MovePlayer(direction, false); // Let go
            return;
        }
        world.MovePlayer(direction, false);
    }

    public string Name => "RAIL";
}
