package gemini.penguinrunner.states;

import gemini.penguinrunner.world.Direction;
import gemini.penguinrunner.world.World;

public class WalkingState implements PlayerState {
    @Override
    public void handleInput(World world, Direction direction) {
        if (direction == null) return;
        if (direction == Direction.UP) return; // Cannot walk up in air unless ladder
        if (direction == Direction.DOWN) {
            world.movePlayer(direction, false);
            return;
        }
        world.movePlayer(direction, true); // Push stones horizontally
    }

    @Override
    public String getName() { return "WALKING"; }
}

