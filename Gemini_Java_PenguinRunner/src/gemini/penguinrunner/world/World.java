package gemini.penguinrunner.world;

import gemini.penguinrunner.ai.Pathfinder;
import gemini.penguinrunner.assets.AudioPlayer;
import gemini.penguinrunner.entities.*;
import gemini.penguinrunner.items.Flamethrower;
import gemini.penguinrunner.items.Item;
import gemini.penguinrunner.items.Teleport;
import gemini.penguinrunner.io.SimpleJson;
import gemini.penguinrunner.states.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure game world encapsulating grid blocks, physics, entities, and turn progression.
 * Completely decoupled from Swing or graphics rendering.
 */
public class World {
    private final File resourceDir;
    private final AudioPlayer audio;
    private final Pathfinder pathfinder = new Pathfinder();

    private final List<String[]> mapLevels = new ArrayList<>();
    private int currentLevelIndex = 0;

    private Block[][] blocks;
    private int rows;
    private int cols;

    private Player player;
    private final List<Enemy> enemies = new ArrayList<>();

    private int totalIceCream = 0;
    private boolean buttonPressed = false;
    private boolean levelCleared = false;
    private boolean gameOver = false;

    private final WalkingState walkingState = new WalkingState();
    private final ClimbingState climbingState = new ClimbingState();
    private final RailState railState = new RailState();
    private final FallingState fallingState = new FallingState();

    public World(File resourceDir, AudioPlayer audio) {
        this.resourceDir = resourceDir;
        this.audio = audio;
        loadMapFile();
    }

    public AudioPlayer audio() { return audio; }
    public Block[][] blocks() { return blocks; }
    public int rows() { return rows; }
    public int cols() { return cols; }
    public Player player() { return player; }
    public List<Enemy> enemies() { return enemies; }
    public int currentLevel() { return currentLevelIndex + 1; }
    public int totalIceCream() { return totalIceCream; }
    public boolean buttonPressed() { return buttonPressed; }
    public boolean isLevelCleared() { return levelCleared; }
    public boolean isGameOver() { return gameOver; }

    public void setGameOver(boolean over) { this.gameOver = over; }

    public boolean isInBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public Block getBlock(int r, int c) {
        return isInBounds(r, c) ? blocks[r][c] : null;
    }

    private void loadMapFile() {
        File mapFile = new File(resourceDir, "maps.json");
        if (!mapFile.exists()) return;

        try {
            String content = Files.readString(mapFile.toPath());
            SimpleJson.JsonValue json = SimpleJson.parse(content);
            if (json instanceof SimpleJson.JsonArray arr) {
                for (SimpleJson.JsonValue entry : arr) {
                    if (entry instanceof SimpleJson.JsonObject obj) {
                        SimpleJson.JsonArray view = obj.getArray("view");
                        if (view == null) view = obj.getArray("map");
                        if (view != null) {
                            String[] lines = new String[view.size()];
                            for (int i = 0; i < view.size(); i++) {
                                lines[i] = view.get(i).asString();
                            }
                            mapLevels.add(lines);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading maps.json: " + e.getMessage());
        }
    }

    public boolean initLevel(int levelIdx) {
        if (mapLevels.isEmpty()) return false;
        currentLevelIndex = Math.max(0, Math.min(levelIdx, mapLevels.size() - 1));
        String[] levelLines = mapLevels.get(currentLevelIndex);

        rows = levelLines.length;
        cols = 0;
        for (String line : levelLines) {
            if (line.length() > cols) cols = line.length();
        }

        blocks = new Block[rows][cols];
        enemies.clear();
        totalIceCream = 0;
        buttonPressed = false;
        levelCleared = false;
        gameOver = false;

        List<Item> preservedItems = (player != null) ? new ArrayList<>(player.items()) : new ArrayList<>();
        int preservedLives = (player != null) ? player.lives() : Player.MAX_LIVES;

        for (int r = 0; r < rows; r++) {
            String line = levelLines[r];
            for (int c = 0; c < cols; c++) {
                char ch = (c < line.length()) ? line.charAt(c) : ' ';
                Block block;

                switch (ch) {
                    case '#' -> block = new Block(TileType.ICE, r, c);
                    case '=' -> block = new Block(TileType.WALL, r, c);
                    case 'H' -> block = new Block(TileType.STAIR, r, c);
                    case '-' -> block = new Block(TileType.RAIL, r, c);
                    case 'S' -> block = new Block(TileType.STONE, r, c);
                    case 'W' -> block = new Block(TileType.WOOD, r, c);
                    case 'D' -> block = new Block(TileType.DOOR, r, c);
                    case 'T', 'b' -> block = new Block(TileType.TRAPDOOR, r, c);
                    case 'B' -> block = new Block(TileType.BUTTON, r, c);
                    case 'G' -> {
                        block = new Block(TileType.ICECREAM, r, c);
                        totalIceCream++;
                    }
                    case 'F' -> block = new Block(TileType.FLAMETHROWER, r, c);
                    case 'P' -> {
                        player = new Player(r, c);
                        player.setLives(preservedLives);
                        for (Item item : preservedItems) player.addItem(item);
                        block = new Block(TileType.BLANK, r, c);
                    }
                    case 'E' -> {
                        enemies.add(new SeekerEnemy(r, c, 1, 1));
                        block = new Block(TileType.BLANK, r, c);
                    }
                    case 'A' -> {
                        enemies.add(new AmbushingEnemy(r, c, 1, 1));
                        block = new Block(TileType.BLANK, r, c);
                    }
                    case 'C' -> {
                        enemies.add(new IceCreamEnemy(r, c, 1, 1));
                        block = new Block(TileType.BLANK, r, c);
                    }
                    default -> block = new Block(TileType.BLANK, r, c);
                }
                blocks[r][c] = block;
            }
        }

        if (player == null) {
            player = new Player(1, 1);
            player.setLives(preservedLives);
        }

        IceAutoTiler.updateAll(blocks);
        updatePlayerState();
        return true;
    }

    public void nextLevel() {
        if (currentLevelIndex + 1 < mapLevels.size()) {
            initLevel(currentLevelIndex + 1);
        } else {
            // Loop back or win
            initLevel(0);
        }
    }

    public void restartLevel() {
        initLevel(currentLevelIndex);
    }

    public void handlePlayerInput(Direction dir) {
        if (gameOver || levelCleared || player == null || player.isDead()) return;

        player.setLastDirection(dir);
        player.state().handleInput(this, dir);

        stepTurn();
    }

    public void passTurn() {
        if (gameOver || levelCleared || player == null || player.isDead()) return;
        stepTurn();
    }

    public void movePlayer(Direction dir, boolean canPush) {
        if (player == null) return;
        int nr = player.row() + dir.dRow();
        int nc = player.col() + dir.dCol();
        if (!isInBounds(nr, nc)) return;

        Block target = blocks[nr][nc];
        if (target != null && target.isPushable() && canPush) {
            if (!tryPushBlock(nr, nc, dir)) {
                return;
            }
        }

        if (canPass(nr, nc)) {
            player.setPosition(nr, nc);
            interactWithTile(nr, nc);
        }
    }

    public void movePlayerDownOne() {
        if (player == null) return;
        int nr = player.row() + 1;
        int nc = player.col();
        if (isInBounds(nr, nc) && canPass(nr, nc)) {
            player.setPosition(nr, nc);
            interactWithTile(nr, nc);
        }
    }

    public boolean canPass(int r, int c) {
        if (!isInBounds(r, c)) return false;
        Block b = blocks[r][c];
        return b == null || !b.isSolid();
    }

    private boolean tryPushBlock(int r, int c, Direction dir) {
        int dc = dir.dCol();
        if (dc == 0) return false;

        int checkCol = c;
        while (isInBounds(r, checkCol) && blocks[r][checkCol].isPushable()) {
            checkCol += dc;
        }

        if (!isInBounds(r, checkCol) || blocks[r][checkCol].isSolid()) {
            return false;
        }

        // Shift blocks
        int fromCol = checkCol - dc;
        while (fromCol != c - dc) {
            blocks[r][fromCol + dc] = blocks[r][fromCol];
            fromCol -= dc;
        }

        blocks[r][c] = new Block(TileType.BLANK, r, c);
        IceAutoTiler.updateDirtyNeighbors(blocks, r, c);
        IceAutoTiler.updateDirtyNeighbors(blocks, r, checkCol);
        return true;
    }

    private void interactWithTile(int r, int c) {
        Block b = blocks[r][c];
        if (b == null) return;

        switch (b.type()) {
            case ICECREAM -> {
                player.addIceCream();
                b.setType(TileType.BLANK);
                audio.playSound("collect");
                checkObjective();
            }
            case FLAMETHROWER -> {
                player.addItem(new Flamethrower());
                b.setType(TileType.BLANK);
                audio.playSound("pickup");
            }
            case TELEPORT -> {
                player.addItem(new Teleport());
                b.setType(TileType.BLANK);
                audio.playSound("pickup");
            }
            case BUTTON -> {
                buttonPressed = !buttonPressed;
                for (int row = 0; row < rows; row++) {
                    for (int col = 0; col < cols; col++) {
                        blocks[row][col].setButtonPressed(buttonPressed);
                    }
                }
                audio.playSound("button");
            }
            case DOOR -> {
                if (b.isDoorOpen()) {
                    levelCleared = true;
                    audio.playSound("door");
                    nextLevel();
                }
            }
            default -> {}
        }
    }

    public void checkObjective() {
        if (player.collectedIceCream() >= totalIceCream) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (blocks[r][c].isDoor()) {
                        blocks[r][c].setType(TileType.DOOR);
                        blocks[r][c].setDoorOpen(true);
                    }
                }
            }
        }
    }

    public void breakBlock(int dCol) {
        if (player == null) return;
        int br = player.row() + 1;
        int bc = player.col() + dCol;

        if (isInBounds(br, bc)) {
            Block b = blocks[br][bc];
            if (b != null && b.type() == TileType.ICE && b.isBreakable()) {
                b.setType(TileType.MOLTEN);
                b.setRegenTurns(6);
                IceAutoTiler.updateDirtyNeighbors(blocks, br, bc);
                audio.playSound("popice");
                stepTurn();
            }
        }
    }

    public void useItem() {
        if (player == null || !player.hasItems()) return;
        Item item = player.getSelectedItem();
        if (item != null) {
            boolean used = item.use(player, this);
            if (used) {
                player.cleanExpiredItems();
                stepTurn();
            }
        }
    }

    public void stepTurn() {
        updateBrokenBlocks();
        moveEnemies();
        checkCollisions();
        updatePlayerState();
    }

    private void updateBrokenBlocks() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Block b = blocks[r][c];
                if (b != null && b.type() == TileType.MOLTEN) {
                    b.decrementRegen();
                    if (b.type() == TileType.ICE) {
                        IceAutoTiler.updateDirtyNeighbors(blocks, r, c);
                    }
                }
            }
        }
    }

    private void moveEnemies() {
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                enemy.tickRevive();
                continue;
            }

            // Check if enemy is in molten block
            Block currBlock = getBlock(enemy.row(), enemy.col());
            if (currBlock != null && currBlock.type() == TileType.MOLTEN) {
                enemy.kill(10);
                continue;
            }

            Direction dir = enemy.decideMove(this, pathfinder);
            if (dir != null) {
                int nr = enemy.row() + dir.dRow();
                int nc = enemy.col() + dir.dCol();
                if (isInBounds(nr, nc) && canPass(nr, nc)) {
                    enemy.setPosition(nr, nc);
                    enemy.setLastDirection(dir);
                }
            }
        }
    }

    public void checkCollisions() {
        if (player == null || player.isDead()) return;

        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && enemy.row() == player.row() && enemy.col() == player.col()) {
                playerDied();
                break;
            }
        }
    }

    public void playerDied() {
        player.loseLife();
        audio.playSound("death");

        if (player.lives() <= 0) {
            gameOver = true;
            audio.playSound("gameover");
        } else {
            resetPositionsAfterDeath();
        }
    }

    private void resetPositionsAfterDeath() {
        player.resetToStart();
        for (Enemy enemy : enemies) {
            enemy.resetToStart();
        }
        updatePlayerState();
    }

    public void updatePlayerState() {
        if (player == null) return;
        int r = player.row();
        int c = player.col();

        Block curr = getBlock(r, c);
        Block below = getBlock(r + 1, c);

        if (curr != null && curr.type() == TileType.STAIR) {
            player.setState(climbingState);
        } else if (curr != null && curr.type() == TileType.RAIL) {
            player.setState(railState);
        } else if (below == null || (!below.isSolid() && below.type() != TileType.STAIR)) {
            player.setState(fallingState);
        } else {
            player.setState(walkingState);
        }
    }
}

