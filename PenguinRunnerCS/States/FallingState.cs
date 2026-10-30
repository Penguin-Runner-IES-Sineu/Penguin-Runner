using iessineu.penguinrunner.Movement;

namespace iessineu.penguinrunner.States;

public class FallingState : PlayerState
{
    public void handleInput(GameState gameState, Direction? direction)
    {
        // Quan cau, ignora la tecla i baixa una casella.
        gameState.movePlayerDownOne();
    }

    public string getName() => "FALLING";
}

