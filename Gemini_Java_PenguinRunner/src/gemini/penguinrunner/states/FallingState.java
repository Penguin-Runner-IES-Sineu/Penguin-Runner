package gemini.penguinrunner.states;

import gemini.penguinrunner.world.Direction;
import gemini.penguinrunner.world.World;

public class FallingState implements PlayerState {
    @Override
    public void handleInput(World world, Direction direction) {
        // While falling, user key presses are ignored; physics pulls down
        world.movePlayerDownOne();
    }

    @Override
    public String getName() { return "FALLING"; }
}

