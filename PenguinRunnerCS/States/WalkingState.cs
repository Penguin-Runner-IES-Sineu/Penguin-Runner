using iessineu.penguinrunner.Movement;

namespace iessineu.penguinrunner.States;

public class WalkingState : PlayerState
{
    public void handleInput(GameState gameState, Direction? direction)
    {
        if (direction == null)
        {
            return;
        }

        // Caminant normal no pot pujar si no està a una escala
        if (direction == Direction.UP)
        {
            return;
        }

        // Pot baixar si la casella és valida
        if (direction == Direction.DOWN)
        {
            gameState.movePlayerBy(direction, false);
            return;
        }

        // Esquerra/dreta: pot moure pedres
        if (direction == Direction.LEFT || direction == Direction.RIGHT)
        {
            gameState.movePlayerBy(direction, true);
        }
    }

    public string getName() => "WALKING";
}

