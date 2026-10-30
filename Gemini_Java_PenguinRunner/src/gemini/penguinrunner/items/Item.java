package gemini.penguinrunner.items;

import gemini.penguinrunner.entities.Player;
import gemini.penguinrunner.world.World;

public interface Item {
    String name();
    int usesLeft();
    boolean isExpired();
    boolean use(Player player, World world);
}

