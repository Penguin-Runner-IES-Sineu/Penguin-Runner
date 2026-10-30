namespace Gemini.PenguinRunner.States;

using Gemini.PenguinRunner.Core;

public class ClimbingState : IPlayerState
{
    public void HandleInput(World world, Direction direction)
    {
        if (direction is Direction.Up or Direction.Down)
        {
            world.MovePlayer(direction, false);
            return;
        }
        world.MovePlayer(direction, true);
    }

    public string Name => "CLIMBING";
}
