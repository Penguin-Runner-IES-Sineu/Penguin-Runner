using iessineu.penguinrunner.Movement;

namespace iessineu.penguinrunner.States;

public class RailState : PlayerState
{
    public void handleInput(GameState gameState, Direction? direction)
    {
        if (direction == null)
        {
            return;
        }

        // A una passarel·la pot anar esquerra/dreta
        if (direction == Direction.LEFT || direction == Direction.RIGHT)
        {
            gameState.movePlayerBy(direction, true);
            return;
        }

        // Pot baixar si hi ha espai
        if (direction == Direction.DOWN)
        {
            gameState.movePlayerBy(direction, false);
            return;
        }

        // Només pot pujar si també està sobre una escala
        if (direction == Direction.UP && gameState.isPlayerOnStair())
        {
            gameState.movePlayerBy(direction, false);
        }
    }

    public string getName() => "ON_RAIL";
}

