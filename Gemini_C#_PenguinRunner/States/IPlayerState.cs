namespace Gemini.PenguinRunner.States;

using Gemini.PenguinRunner.Core;

public interface IPlayerState
{
    void HandleInput(World world, Direction direction);
    string Name { get; }
}
