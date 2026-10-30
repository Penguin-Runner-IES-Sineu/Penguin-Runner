package gemini.penguinrunner.items;

import gemini.penguinrunner.entities.Player;
import gemini.penguinrunner.world.Block;
import gemini.penguinrunner.world.Direction;
import gemini.penguinrunner.world.TileType;
import gemini.penguinrunner.world.World;

public class Flamethrower implements Item {
    private int usesLeft;

    public Flamethrower() {
        this.usesLeft = 3;
    }

    public Flamethrower(int usesLeft) {
        this.usesLeft = usesLeft;
    }

    @Override
    public String name() { return "flamethrower"; }

    @Override
    public int usesLeft() { return usesLeft; }

    @Override
    public boolean isExpired() { return usesLeft <= 0; }

    @Override
    public boolean use(Player player, World world) {
        if (usesLeft <= 0) return false;
        usesLeft--;

        Direction dir = player.lastDirection();
        int r = player.row();
        int c = player.col();

        for (int step = 1; step <= 3; step++) {
            int tr = r + dir.dRow() * step;
            int tc = c + dir.dCol() * step;
            if (!world.isInBounds(tr, tc)) break;

            Block b = world.getBlock(tr, tc);
            if (b == null) continue;

            if (b.type() == TileType.WOOD) {
                b.setType(TileType.BURNT);
                b.setBurning(true);
            } else if (b.type() == TileType.ICE || b.type() == TileType.MOLTEN) {
                b.setType(TileType.MOLTEN);
                b.setRegenTurns(8); // Melted ice trap
            } else if (b.isSolid()) {
                break; // Wall blocks flame progression
            }
        }
        world.audio().playSound("flame");
        return true;
    }
}

