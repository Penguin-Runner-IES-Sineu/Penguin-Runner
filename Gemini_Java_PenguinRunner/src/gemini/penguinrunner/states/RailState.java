package gemini.penguinrunner.states;

import gemini.penguinrunner.world.Direction;
import gemini.penguinrunner.world.World;

public class RailState implements PlayerState {
    @Override
    public void handleInput(World world, Direction direction) {
        if (direction == null) return;
        if (direction == Direction.UP) return; // Cannot move up while hanging on rail
        if (direction == Direction.DOWN) {
            world.movePlayer(direction, false); // Let go of rail and fall/descend
            return;
        }
        world.movePlayer(direction, false); // Shimmy horizontally
    }

    @Override
    public String getName() { return "RAIL"; }
}

