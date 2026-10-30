namespace Gemini.PenguinRunner.States;

using Gemini.PenguinRunner.Core;

public class FallingState : IPlayerState
{
    public void HandleInput(World world, Direction direction)
    {
        world.MovePlayerDownOne();
    }

    public string Name => "FALLING";
}
