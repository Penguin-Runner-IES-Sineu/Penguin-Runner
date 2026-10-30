using iessineu.penguinrunner.Movement;

namespace iessineu.penguinrunner.States;

public class ClimbingState : PlayerState
{
    public void handleInput(GameState gameState, Direction? direction)
    {
        if (direction == null)
        {
            return;
        }

        // En una escala pot pujar i baixar
        if (direction == Direction.UP || direction == Direction.DOWN)
        {
            gameState.movePlayerBy(direction, false);
            return;
        }

        // També pot sortir lateralment de l'escala
        if (direction == Direction.LEFT || direction == Direction.RIGHT)
        {
            gameState.movePlayerBy(direction, true);
        }
    }

    public string getName() => "CLIMBING";
}

