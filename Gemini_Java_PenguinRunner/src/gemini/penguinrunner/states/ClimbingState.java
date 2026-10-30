package gemini.penguinrunner.states;

import gemini.penguinrunner.world.Direction;
import gemini.penguinrunner.world.World;

public class ClimbingState implements PlayerState {
    @Override
    public void handleInput(World world, Direction direction) {
        if (direction == null) return;
        if (direction == Direction.UP || direction == Direction.DOWN) {
            world.movePlayer(direction, false);
            return;
        }
        world.movePlayer(direction, true);
    }

    @Override
    public String getName() { return "CLIMBING"; }
}

