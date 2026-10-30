package gemini.penguinrunner.states;

import gemini.penguinrunner.world.Direction;
import gemini.penguinrunner.world.World;

public interface PlayerState {
    void handleInput(World world, Direction direction);
    String getName();
}

