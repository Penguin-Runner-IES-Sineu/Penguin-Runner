package gemini.penguinrunner.entities;

import java.util.ArrayList;
import java.util.List;

import gemini.penguinrunner.items.Item;
import gemini.penguinrunner.states.PlayerState;
import gemini.penguinrunner.states.WalkingState;

public class Player extends Entity {
    public static final int MAX_LIVES = 3;

    private int lives = MAX_LIVES;
    private int collectedIceCream = 0;
    private PlayerState state;
    private final List<Item> items = new ArrayList<>();
    private int selectedItemIndex = 0;

    public Player(int row, int col) {
        super(row, col);
        this.state = new WalkingState();
    }

    public int lives() { return lives; }
    public void setLives(int lives) { this.lives = Math.max(0, lives); }
    public void loseLife() { if (lives > 0) lives--; }
    public boolean isAlive() { return lives > 0; }
    public void resetLives() { this.lives = MAX_LIVES; }

    public int collectedIceCream() { return collectedIceCream; }
    public void addIceCream() { collectedIceCream++; }
    public void setCollectedIceCream(int count) { this.collectedIceCream = count; }

    public PlayerState state() { return state; }
    public void setState(PlayerState state) { this.state = state; }

    public List<Item> items() { return items; }
    public boolean hasItems() { return !items.isEmpty(); }
    public int selectedItemIndex() { return selectedItemIndex; }
    public void setSelectedItemIndex(int index) {
        if (items.isEmpty()) {
            selectedItemIndex = 0;
        } else {
            selectedItemIndex = ((index % items.size()) + items.size()) % items.size();
        }
    }

    public Item getSelectedItem() {
        if (items.isEmpty()) return null;
        if (selectedItemIndex < 0 || selectedItemIndex >= items.size()) {
            selectedItemIndex = 0;
        }
        return items.get(selectedItemIndex);
    }

    public void cycleItem(int offset) {
        if (items.isEmpty()) return;
        selectedItemIndex = ((selectedItemIndex + offset) % items.size() + items.size()) % items.size();
    }

    public void addItem(Item item) {
        if (item != null) items.add(item);
    }

    public void cleanExpiredItems() {
        items.removeIf(Item::isExpired);
        if (selectedItemIndex >= items.size() && !items.isEmpty()) {
            selectedItemIndex = items.size() - 1;
        }
    }

    @Override
    public void resetToStart() {
        super.resetToStart();
        this.state = new WalkingState();
    }
}

