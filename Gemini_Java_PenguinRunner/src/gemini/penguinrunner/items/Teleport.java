package gemini.penguinrunner.items;

import gemini.penguinrunner.entities.Player;
import gemini.penguinrunner.world.World;

public class Teleport implements Item {
    private boolean beaconSet = false;
    private int beaconRow = -1;
    private int beaconCol = -1;
    private boolean used = false;

    public Teleport() {}

    @Override
    public String name() { return "teleport"; }

    @Override
    public int usesLeft() { return used ? 0 : (beaconSet ? 1 : 2); }

    @Override
    public boolean isExpired() { return used; }

    public boolean isBeaconSet() { return beaconSet; }
    public int beaconRow() { return beaconRow; }
    public int beaconCol() { return beaconCol; }

    @Override
    public boolean use(Player player, World world) {
        if (used) return false;

        if (!beaconSet) {
            beaconRow = player.row();
            beaconCol = player.col();
            beaconSet = true;
            world.audio().playSound("pickup");
            return true;
        } else {
            // Teleport to beacon
            player.setPosition(beaconRow, beaconCol);
            used = true;
            world.audio().playSound("teleport");
            return true;
        }
    }
}

