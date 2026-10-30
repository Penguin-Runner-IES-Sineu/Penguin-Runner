using iessineu.penguinrunner.Movement;

namespace iessineu.penguinrunner.States;

public interface PlayerState
{
    void handleInput(GameState gameState, Direction? direction);
    string getName();

    void HandleInput(GameState gameState, Direction? direction) => handleInput(gameState, direction);
    string GetName() => getName();
}

