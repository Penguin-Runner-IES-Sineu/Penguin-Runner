namespace Gemini.PenguinRunner.States;

using Gemini.PenguinRunner.Core;

public class WalkingState : IPlayerState
{
    public void HandleInput(World world, Direction direction)
    {
        if (direction == Direction.Up) return;
        if (direction == Direction.Down)
        {
            world.MovePlayer(direction, false);
            return;
        }
        world.MovePlayer(direction, true); // Push stones horizontally
    }

    public string Name => "WALKING";
}
