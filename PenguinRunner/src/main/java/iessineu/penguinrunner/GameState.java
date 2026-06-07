/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

/**
 *
 * @author loren
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import iessineu.penguinrunner.Blocks.Block;
import iessineu.penguinrunner.Blocks.TileType;
import iessineu.penguinrunner.Entity.GameMap;
import iessineu.penguinrunner.Entity.Items.Item;
import iessineu.penguinrunner.Entity.Items.Teleport;
import iessineu.penguinrunner.Entity.Player;
import iessineu.penguinrunner.Entity.enemies.AI;
import iessineu.penguinrunner.Entity.enemies.AmbushingEnemy;
import iessineu.penguinrunner.Entity.enemies.Enemy;
import iessineu.penguinrunner.Entity.enemies.IceCreamEnemy;
import iessineu.penguinrunner.Entity.enemies.SeekerEnemy;
import iessineu.penguinrunner.Movement.Direction;
import iessineu.penguinrunner.Movement.PositionHistory;
import iessineu.penguinrunner.States.ClimbingState;
import iessineu.penguinrunner.States.FallingState;
import iessineu.penguinrunner.States.PlayerState;
import iessineu.penguinrunner.States.RailState;
import iessineu.penguinrunner.States.WalkingState;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;

public class GameState implements Serializable {

    private ArrayList<PositionHistory> lastPositions = new ArrayList();
    private List<BrokenBlock> brokenBlocks;
    private List<Block> stones;
    // private String rutaMapes = "resources/maps.json";
    private final String rutaMapes = GamePanel.getFolderPath() + "maps.json";
    private int nivellActual = 0;
    // private long lastTurn;
    private final AI buscador = new AI();
    private List<GameMap> mapList = llegirMapes(rutaMapes);
    private GameMap mapObject = mapList.get(0);
    private Player player;
    private List<Enemy> enemies;
    private int iceCream = 0;
    private int lives = 3;
    private int startPlayerRow;
    private int startPlayerCol;
    private boolean moddedMusic = false;
    private boolean buttonPressed = false;

    private SoundManager soundManager = new SoundManager();
    private PlayerState walkingState = new WalkingState();
    private PlayerState climbingState = new ClimbingState();
    private PlayerState railState = new RailState();
    private PlayerState fallingState = new FallingState();

    private static final int NOT_VISITED = -1;
    private static final int BLOCKED = -2;

    private Timer fallingTimer;

    public void resetTimer() {
        fallingTimer = new Timer(150, e -> {
            if (player.getState() == fallingState) {
                movePlayerDownOne();
                updatePlayerState();
                updateLogic();
                takeTurn();
            }
        });
    }

    private Block[][] blocks = loadMap();

    public Block[][] loadMap() {
        resetTimer();
        List<Item> savedItemList = new LinkedList();
        if (player != null) {
            savedItemList = player.getItems();
        }
        String[] level = mapObject.getMap();
        blocks = new Block[level.length][level[0].length()];
        brokenBlocks = new ArrayList();
        stones = new ArrayList<>();
        enemies = new ArrayList();
        player = null;
        startPlayerRow = 0;
        startPlayerCol = 0;
        iceCream = 0;
        buttonPressed = false;

        for (int row = 0; row < level.length; row++) {
            for (int col = 0; col < level[row].length(); col++) {
                char symbol = level[row].charAt(col);
                switch (symbol) {
                    case '#' -> {
                        blocks[row][col] = new Block(TileType.WALL);
                    }
                    case '.' -> {
                        blocks[row][col] = new Block(TileType.ICE);
                    }
                    case 'H' -> {
                        blocks[row][col] = new Block(TileType.STAIR);
                    }
                    case '-' -> {
                        blocks[row][col] = new Block(TileType.RAIL);
                    }
                    case 'D' -> {
                        blocks[row][col] = new Block(TileType.DOOR);
                    }
                    case 'S' -> {
                        blocks[row][col] = new Block(TileType.STONE);
                        stones.add(blocks[row][col]);
                    }
                    case 'B' -> {
                        blocks[row][col] = new Block(TileType.BUTTON);
                    }
                    case 'b' -> {
                        blocks[row][col] = new Block(TileType.TRAPDOOR);
                    }
                    case 'G' -> {
                        blocks[row][col] = new Block(TileType.ICECREAM);
                        iceCream++;
                    }
                    case 'F' -> {
                        blocks[row][col] = new Block(TileType.FLAMETHROWER);
                    }
                    case 'T' -> {
                        blocks[row][col] = new Block(TileType.TELEPORT);
                    }
                    case 'P' -> {
                        player = new Player(row, col);
                        startPlayerRow = row;
                        startPlayerCol = col;
                        blocks[row][col] = new Block(TileType.BLANK);
                        saveLastPosition();
                    }
                    case 'E' -> {
                        enemies.add(new Enemy(row, col, 1, 1));
                        blocks[row][col] = new Block(TileType.BLANK);
                    }
                    case 'A' -> {
                        enemies.add(new AmbushingEnemy(row, col, 1, 1));
                        blocks[row][col] = new Block(TileType.BLANK);
                    }
                    case 'W' -> {
                        enemies.add(new SeekerEnemy(row, col, 1, 1));
                        blocks[row][col] = new Block(TileType.WOOD);
                    }
                    case 'C' -> {
                        enemies.add(new IceCreamEnemy(row, col, 1, 1));
                        blocks[row][col] = new Block(TileType.BLANK);
                    }
                    default -> {
                        blocks[row][col] = new Block(TileType.BLANK);
                    }
                }
            }
        }
        player.setItems(savedItemList);
        updatePlayerState();

        return blocks;
    }

    private int[][] createPathMapForAI(Enemy currentEnemy) {
        int[][] pathMap = new int[getRows()][getCols()];

        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                boolean occupiedByAnotherEnemy = isEnemy(row, col)
                        && !(currentEnemy.getRow() == row && currentEnemy.getCol() == col);

                boolean molten = isMolten(row, col);

                if ((isSolid(row, col) && !molten) || occupiedByAnotherEnemy) {
                    pathMap[row][col] = BLOCKED;
                } else {
                    pathMap[row][col] = NOT_VISITED;
                }
            }
        }

        return pathMap;
    }

    public void reloadSprites() {
        resetTimer();
        // fallingTimer.start();
        mapObject = mapList.get(nivellActual);
        Block[][] mapa = getBlocks();
        // List<Block> newStoneList = new ArrayList();
        // Block[][] mapaNou = new Block[mapa.length][mapa[0].length];
        for (int row = 0; row < mapa.length; row++) {
            for (int col = 0; col < mapa[row].length; col++) {
                Block blocAntic = mapa[row][col];
                blocAntic.setPrintables();
                // mapaNou[row][col] = new Block(blocAntic.getType());
                // if (blocAntic.isDoor()) {
                //     mapaNou[row][col].setDoor(true);
                // }
                // if (blocAntic.getType() == TileType.STONE) {
                //     newStoneList.add(mapaNou[row][col]);
                // }
            }
        }
        // List<Enemy> newEnemyList = new ArrayList();
        for (Enemy enemy : getEnemies()) {
            // boolean isDead = enemy.isDead();
            // int ttr = enemy.getTimeToRevive();
            // enemy = new Enemy(enemy.getRow(), enemy.getCol(), enemy.getRespawnCol(), enemy.getRespawnRow());
            // if (isDead) {
            //     enemy.die();
            // }
            // enemy.setTimeToRevive(ttr);
            // newEnemyList.add(enemy);
            enemy.setPrintables();
        }
        // enemies = newEnemyList;
        // int iceCreams = player.geticeCream();
        player.setPrintables();
        // player = new Player(player.getRow(), player.getCol());
        // player.setIceCream(iceCreams);
        // stones = newStoneList;
        // blocks = mapaNou;
        updatePlayerState();
    }

    /*
     * TORNS
     */
    public void takeTurn(Direction direction) {
        if (player.getState() == fallingState) {
            if (!fallingTimer.isRunning()) {
                fallingTimer.start();
            }
        } else if (direction != null) {
            fallingTimer.stop();
            player.setLastDirection(direction);
            player.getState().handleInput(this, direction);
            updatePlayerState();
            updateLogic();
            if (player.getState() == fallingState) {
                takeTurn();
            }
        }
    }

    public void changeItem(boolean isLeft) {
        if (player.hasItems()) {
            if (isLeft) {
                player.previousItem();
            } else {
                player.nextItem();
            }
            System.out.println("Item seleccionat: " + player.getSelectedItem());
        } else {
            System.out.println("No tens cap item!");
        }
    }

    public void takeTurn() {
        takeTurn(null);
    }

    public void updateLogic() {
        saveLastPosition();
        collectItem();
        moveBlocks();
        updateBrokenBlocks();
        moveEnemies();
        checkCollisions();
        updatePlayerState();
    }

    public void useItem() {
        Item i = player.getSelectedItem();
        if (player.hasItems()) {
            player.useItem(this);
        } else {
            System.out.println("No tens objectes!");
        }
        switch (i.getName()) {
            case "flamethrower" -> {
                soundManager.playSound("flame", !GamePanel.hasGame());
            }
            case "teleport" -> {
                Teleport t = (Teleport) i;
                boolean moved = t.use(player);
                if (moved) {
                    blocks[t.getRow()][t.getCol()] = new Block(TileType.BLANK);
                    soundManager.playSound("teleport", !GamePanel.hasGame());
                    player.removeItem();
                } else {
                    blocks[t.getRow()][t.getCol()] = new Block(TileType.TELEPORT);
                    blocks[t.getRow()][t.getCol()].setCollectable(false);
                }
                blocks[t.getRow()][t.getCol()].setPrintables();
            }
        }
    }

    /*
     * MÀQUINA D'ESTATS DEL JUGADOR
     */
    private void updatePlayerState() {
        if (shouldPlayerDrop()) {
            player.setState(fallingState);
        } else if (isStair(player.getRow(), player.getCol())) {
            player.setState(climbingState);
        } else if (isRail(player.getRow(), player.getCol())) {
            player.setState(railState);
        } else {
            player.setState(walkingState);
        }
    }

    public boolean isPlayerOnStair() {
        return isStair(player.getRow(), player.getCol());
    }

    public void movePlayerBy(Direction direction, boolean canPushStone) {
        int row = player.getRow();
        int col = player.getCol();

        int nextRow = row + direction.getDr();
        int nextCol = col + direction.getDc();

        if (canPushStone && isStone(nextRow, nextCol)) {
            boolean pushed = tryPushStone(row, col, direction);
            if (!pushed) {
                return;
            }
        }

        if (canMoveTo(nextRow, nextCol)) {
            player.setPosition(nextRow, nextCol);
        }
    }

    public void movePlayerDownOne() {
        int nextRow = player.getRow() + 1;
        int nextCol = player.getCol();

        if (canMoveTo(nextRow, nextCol)) {
            player.setPosition(nextRow, nextCol);
        }
    }

    private boolean shouldPlayerDrop() {
        int row = player.getRow();
        int col = player.getCol();
        if (isEnemy(row + 1, col)) {
            // System.out.println("Tens un enemic davall");
            return false;
        }

        return !isSolid(row + 1, col)
                && !isRail(row, col)
                && !isStair(row, col)
                && !isStair(row + 1, col);
        // && !isEnemy(row + 1, col);
    }

    /*
     * PEDRES
     */
    private boolean tryPushStone(int row, int playerCol, Direction direction) {
        int dc = direction.getDc();

        if (dc == 0) {
            return false;
        }

        int firstStoneCol = playerCol + dc;
        int checkCol = firstStoneCol;

        while (!isOutOfBounds(row, checkCol) && isStone(row, checkCol)) {
            checkCol += dc;
        }

        if (isOutOfBounds(row, checkCol) || !isBlank(row, checkCol)) {
            return false;
        }

        int col = checkCol - dc;

        while (col != playerCol) {
            Block blockToMove = blocks[row][col];
            blocks[row][col] = new Block(TileType.BLANK);
            blocks[row][col + dc] = blockToMove;

            col -= dc;
        }

        return true;
    }

    private void moveBlocks() {
        for (int row = blocks.length - 1; row >= 0; row--) {
            for (int col = 0; col < blocks[row].length; col++) {
                if (blocks[row][col].getType() == TileType.STONE) {
                    int nextRow = row + 1;

                    if (isOutOfBounds(nextRow, col)) {
                        continue;
                    }

                    if (!isBlank(nextRow, col)) {
                        continue;
                    }

                    if (player.getRow() == nextRow && player.getCol() == col) {
                        continue;
                    }

                    if (isEnemy(nextRow, col)) {
                        continue;
                    }

                    blocks[nextRow][col] = blocks[row][col];
                    blocks[row][col] = new Block(TileType.BLANK);
                }
            }
        }
    }

    /*
     * ACCIONS
     */
    public void breakDownLeft() {
        if (canMoveTo(player.getRow(), player.getCol() - 1)) {
            breakBlock(player.getRow() + 1, player.getCol() - 1);
            updateLogic();
        }
    }

    public void breakDownRight() {
        if (canMoveTo(player.getRow(), player.getCol() + 1)) {
            breakBlock(player.getRow() + 1, player.getCol() + 1);
            updateLogic();
        }
    }

    public void breakBlock(int row, int col) {
        if (isOutOfBounds(row, col)) {
            return;
        }

        Block block = blocks[row][col];

        if (block != null && block.isBreakable()) {
            blocks[row][col] = new Block(TileType.MOLTEN);
            brokenBlocks.add(new BrokenBlock(row, col, 5));
        }
    }

    private void updateBrokenBlocks() {
        for (int i = brokenBlocks.size() - 1; i >= 0; i--) {
            BrokenBlock block = brokenBlocks.get(i);

            block.turnsLeft--;

            if (block.turnsLeft <= 0) {
                blocks[block.row][block.col] = new Block(TileType.ICE);
                brokenBlocks.remove(i);
            }
        }
    }

    /*
     * ENEMICS
     */
    private void moveEnemies() {
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                enemy.subtractTimeToRevive(1);

                if (enemy.getTimeToRevive() <= 0) {
                    if (!isEnemy(enemy.getRespawnRow(), enemy.getRespawnRow())) {
                        enemy.revive();
                    }
                }

                continue;
            }
            if (isEnemy(enemy.getRow(), enemy.getCol() + 1)) {

            }

            if (!isMolten(enemy.getRow(), enemy.getCol())) {
                moveEnemy(enemy);
            }
        }
    }

    private void moveEnemy(Enemy enemy) {
        int row = enemy.getRow();
        int col = enemy.getCol();

        // **** //
        if (shouldDie(row, col)) {
            dropIceCreamIfNeeded(enemy);
            enemy.die();
            enemy.setTimeToRevive(7);
            return;
        }

        if (shouldEnemyDrop(row, col)) {
            if (!isEnemy(row + 1, col)) {
                enemy.setPosition(row + 1, col);
            }
            return;
        }
        // **** //
        if (enemy instanceof IceCreamEnemy iceCreamEnemy) {
            moveIceCreamEnemy(iceCreamEnemy);
            return;
        }

        if (enemy instanceof AmbushingEnemy ambushingEnemy) {
            int enemyRow = ambushingEnemy.getRow();
            int enemyCol = ambushingEnemy.getCol();

            // **** //
            PositionHistory easyTarget = canReachEasy(
                    enemyRow,
                    enemyCol,
                    player.getRow(),
                    player.getCol()
            );

            // **** //
            PositionHistory target;

            // **** //
            if (easyTarget != null) {
                target = easyTarget;
            } else {
                target = getAmbushingTarget();
            }

            Direction direction = buscador.getShortestDirection(
                    createPathMapForAI(ambushingEnemy),
                    createTileMapForAI(),
                    enemyRow,
                    enemyCol,
                    target.getX(),
                    target.getY()
            );

            if (direction == null) {
                return;
            }

            int nextRow = ambushingEnemy.getRow() + direction.getDr();
            int nextCol = ambushingEnemy.getCol() + direction.getDc();

            if (isEnemy(nextRow, nextCol)) {
                return;
            }

            if ((canMoveTo(nextRow, nextCol) && !isEnemy(nextRow, nextCol)
                    || isMolten(nextRow, nextCol))
                    ) {
                ambushingEnemy.setPosition(nextRow, nextCol);
            }

            return;
        }

        if (enemy instanceof SeekerEnemy seekerEnemy) {
            Direction direction = buscador.getShortestDirection(
                    createPathMapForAI(seekerEnemy),
                    createTileMapForAI(),
                    seekerEnemy.getRow(),
                    seekerEnemy.getCol(),
                    player.getRow(),
                    player.getCol()
            );

            if (direction == null) {
                return;
            }

            int nextRow = seekerEnemy.getRow() + direction.getDr();
            int nextCol = seekerEnemy.getCol() + direction.getDc();

            if (isEnemy(nextRow, nextCol)) {
                return;
            }

            if ((canMoveTo(nextRow, nextCol)
                    || isMolten(nextRow, nextCol))
                    && !isEnemy(nextRow, nextCol)) {
                seekerEnemy.setPosition(nextRow, nextCol);
            }

            return;
        }
        int dr = 0;
        int dc = 0;

        if (enemy.getRow() < player.getRow()) {
            dr = 1;
        } else if (enemy.getRow() > player.getRow()) {
            if (isStair(enemy.getRow(), enemy.getCol())) {
                dr = -1;
            }
        } else if (enemy.getCol() < player.getCol()) {
            dc = 1;
        } else if (enemy.getCol() > player.getCol()) {
            dc = -1;
        }

        int nextRow = enemy.getRow() + dr;
        int nextCol = enemy.getCol() + dc;

        if (canMoveTo(nextRow, nextCol) && !isEnemy(nextRow, nextCol)) {
            enemy.setPosition(nextRow, nextCol);
        }
    }

    private boolean shouldEnemyDrop(int row, int col) {
        return !isSolid(row + 1, col)
                && !isRail(row, col)
                && !isStair(row, col)
                && !isStair(row + 1, col)
                && !isEnemy(row + 1, col);
    }

    private boolean shouldDie(int row, int col) {
        return isIce(row, col);
    }

    /*
     * OBJECTES I COL·LISIONS
     */
    private void collectItem() {
        int row = player.getRow();
        int col = player.getCol();

        Block block = blocks[row][col];

        if (block != null && block.isCollectable()) {
            // if (player.hasItem(block.getType().toString())) {
            // System.out.println("Ja tens un item d'aquest tipus!");
            // } else {
            // }
            if (!player.hasItem(block.getType().toString().toLowerCase())) {
                if (block.getType() != TileType.ICECREAM) {
                    soundManager.playSound("pickup", !GamePanel.hasGame());
                }
                blocks[row][col] = new Block(TileType.BLANK);
                switch (block.getType()) {
                    case TileType.ICECREAM -> {
                        soundManager.playSound("collect", !GamePanel.hasGame());
                        player.addIceCream();
                    }
                    case TileType.FLAMETHROWER -> {
                        player.addItem("flamethrower");
                    }
                    case TileType.TELEPORT -> {
                        player.addItem("teleport");
                    }
                }
            }
        }
    }

    private void checkCollisions() {
        int playerRow = player.getRow();
        int playerCol = player.getCol();
        boolean dead = false;

        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && isEnemy(playerRow, playerCol)) {
                // System.out.println("Condicio 1");
                dead = true;
            }
            if (!enemy.isDead() && isEnemy(playerRow, playerCol) && blocks[enemy.getRow()][enemy.getCol()].getType() != TileType.MOLTEN) {
                // System.out.println("Condicio 2");
                dead = true;
            }
            if (dead) {
                System.out.println("You died");
                playerDied(false);
                return;
            }
        }

        if (isIce(playerRow, playerCol) || isFire(playerRow, playerCol)) {
            System.out.println("You died");
            playerDied(false);
        }
    }

    public void playerDied(boolean wasForced) {
        player.loseLife();
//    soundManager.playSound("die", !GamePanel.hasGame()); si tienes el sonido

        if (player.isAlive()) {
            // Solo recarga el nivel, conserva las vidas
            if (!wasForced) {
                soundManager.playSound("death", !GamePanel.hasGame());
            }
            List<Item> items = player.getItems();
            int lives = player.getLives();
            loadMap();
            player.setItems(items);       // conserva ítems si quieres
            // o player.setItems(new ArrayList()); para perderlos
            player.setLives(lives);       // restaura las vidas tras loadMap
        } else {
            soundManager.playSound("gameover", !GamePanel.hasGame());
            // Game Over: reinicia desde el nivel 0
            gameOver();
        }
    }

    private void gameOver() {
        nivellActual = 0;
        mapObject = mapList.get(0);
        loadMap();
        player.setItems(new ArrayList<>());
        // Las vidas se resetean solas en loadMap si añades lives=MAX_LIVES allí
    }

    /*
     * CONSULTES DE BLOCS
     */
    private boolean canMoveTo(int row, int col) {
        if (blocks[row][col].getType() == TileType.MOLTEN && isEnemy(row, col)) {
            return false;
        }
        return !isOutOfBounds(row, col)
                && !isSolid(row, col);
    }

    private boolean isOutOfBounds(int row, int col) {
        return row < 0
                || row >= getRows()
                || col < 0
                || col >= getCols();
    }

    private Block getBlock(int row, int col) {
        if (isOutOfBounds(row, col)) {
            return new Block(TileType.BLANK);
        }

        return blocks[row][col];
    }

    private boolean isBlank(int row, int col) {
        return !isOutOfBounds(row, col) && blocks[row][col].getType() == TileType.BLANK;
    }

    private boolean isSolid(int row, int col) {
        if (isOutOfBounds(row, col)) {
            return true;
        }

        Block block = blocks[row][col];

        return block != null && block.isSolid();
    }

    private boolean isIce(int row, int col) {
        Block block = getBlock(row, col);

        return block != null && block.getType() == TileType.ICE;
    }

    private boolean isFire(int row, int col) {
        Block block = getBlock(row, col);
        return block.isFire();
    }

    private boolean isRail(int row, int col) {
        Block block = getBlock(row, col);

        return block != null && block.isRail();
    }

    private boolean isStair(int row, int col) {
        Block block = getBlock(row, col);

        return block != null && block.isClimbable();
    }

    private boolean isStone(int row, int col) {
        Block block = getBlock(row, col);

        return block != null && block.isPushable();
    }

    private boolean isMolten(int row, int col) {
        Block block = getBlock(row, col);

        return block != null && block.getType() == TileType.MOLTEN;
    }

    private boolean isEnemy(int row, int col) {
        for (Enemy enemy : enemies) {
            if (!enemy.isDead()
                    && enemy.getRow() == row
                    && enemy.getCol() == col) {
                return true;
            }
        }

        return false;
    }

    /*
     * GETTERS
     */
    public TileType getType(int row, int col) {
        return getBlock(row, col).getType();
    }

    public int getRows() {
        return blocks.length;
    }

    public int getCols() {
        return blocks[0].length;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public Block[][] getBlocks() {
        return blocks;
    }

    public void setBlock(int row, int col, Block block) {
        blocks[row][col] = block;
    }

    void interact() {
        Block bloc = blocks[player.getRow()][player.getCol()];
        if (bloc.getType() == TileType.DOOR) {
            soundManager.playSound("door", !GamePanel.hasGame());
            nivellActual++;
            mapObject = mapList.get(nivellActual);
            loadMap();
        }
        if (bloc.getType() == TileType.BUTTON) {
            buttonPressed = !buttonPressed;
        }
    }

    public boolean checkObjective() {
        return player.geticeCream() >= iceCream;
    }

    public boolean buttonPressed() {
        return buttonPressed;
    }

    public int getNivell() {
        return nivellActual;
    }

    public List<Block> getStones() {
        return stones;
    }

    public void setStones(List<Block> stones) {
        this.stones = stones;
    }

    public List<GameMap> getMapList() {
        return mapList;
    }

    public void setMapList(List<GameMap> mapList) {
        this.mapList = mapList;
    }

    public boolean isModdedMusic() {
        return moddedMusic;
    }

    public void setModdedMusic(boolean moddedMusic) {
        this.moddedMusic = moddedMusic;
    }

    public void increaseVolume() {
        float vol = soundManager.getVolume();
        soundManager.setVolume(vol + 0.05f);
    }

    public void decreaseVolume() {
        float vol = soundManager.getVolume();
        soundManager.setVolume(vol - 0.05f);
    }

    public void changeVolume() {
        String vol = JOptionPane.showInputDialog("Introdueix el volum que vol, com un valor entre 0 i 100"
        );
        vol = vol.replace(",", ".");
        float volume = Float.parseFloat(vol);
        changeVolume(volume);
    }

    public void changeVolume(float vol) {
        soundManager.setVolume(vol / 100);
    }

    /*
     * BLOCS ROMPUTS
     */
    private static class BrokenBlock implements Serializable {

        int row;
        int col;
        int turnsLeft;

        BrokenBlock(int row, int col, int turnsLeft) {
            this.row = row;
            this.col = col;
            this.turnsLeft = turnsLeft;
        }
    }

    /*
     * LECTURA MAPES
     */
    public List<GameMap> llegirMapes(String rutaMapes) {

        JSONArray maps = new JSONArray(llegirMapaJSON(!GamePanel.hasGame()));
        List<GameMap> llista = new ArrayList();

        for (int i = 0; i < maps.length(); i++) {
            JSONObject obj = maps.getJSONObject(i);
            JSONArray jsonView = obj.getJSONArray("view");
            int mapIndex = obj.getInt("level") - 1;

            String[] view = new String[jsonView.length()];

            for (int j = 0; j < view.length; j++) {
                view[j] = jsonView.getString(j);
            }

            GameMap map = new GameMap(view);
            if (mapIndex < llista.size() && llista.get(mapIndex) != null) {
                llista.set(mapIndex, map);
            } else {
                llista.add(map);
            }
        }

        return llista;
    }

    public String llegirMapaJSON(boolean fromResource) {
        String json = "";
        BufferedReader fitxer;
        ClassLoader classLoader = PenguinRunner.class.getClassLoader();
        InputStream is = classLoader.getResourceAsStream("maps.json");
        System.out.print("Carregant Mapes:");
        try {
            if (fromResource) {
                System.out.println("Carregant Recurs");
                fitxer = new BufferedReader(new InputStreamReader(is));
            } else {
                System.out.println("Carregant Fitxer");
                fitxer = new BufferedReader(new FileReader(GamePanel.getFolderPath() + "maps.json"));
            }
            json = fitxerJSON(fitxer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;

    }

    public String fitxerJSON(BufferedReader fitxer) {
        if (fitxer == null) {
            return "Hi ha hagut un error";
        }
        String jsonString = "";
        try {
            String line;

            while ((line = fitxer.readLine()) != null) {
                jsonString += line;
            }

            fitxer.close();

        } catch (IOException ex) {
            System.out.println("Problema d'entrada i sortida");
        }
        return jsonString;
    }

    public void addMod(String modType, JSONObject mod, String modPath) {
        switch (modType) {
            case "map" -> {
                addModMap(mod);
            }
            case "sprite" -> {
                String originalFilename = mod.getString("filename");
                mod.put("filename", modPath + "/" + originalFilename);
                addModSprite(mod);
            }
            case "sfx" -> {
                String originalFilename = mod.getString("filename");
                mod.put("filename", modPath + "/" + originalFilename);
                addModSound(mod);
            }
            case "music" -> {
                String originalFilename = mod.getString("filename");
                mod.put("filename", modPath + "/" + originalFilename);
                addModMusic(mod);
            }
        }
    }

    public void addModMap(JSONObject modMap) {
        JSONArray jsonView = modMap.getJSONArray("view");
        int mapIndex = modMap.getInt("level") - 1;

        String[] view = new String[jsonView.length()];

        for (int j = 0; j < view.length; j++) {
            view[j] = jsonView.getString(j);
        }

        GameMap map = new GameMap(view);
        if (mapIndex < mapList.size() && mapList.get(mapIndex) != null) {
            mapList.set(mapIndex, map);
        } else {
            mapList.add(map);
        }
    }

    private void addModSprite(JSONObject mod) {
        Map<String, List<String>> spriteMap = GamePanel.getSpriteMap();
        String type = "";
        List<String> atributs = new ArrayList();
        try {
            type = mod.getString("type");
        } catch (JSONException e) {
            System.out.println("No s'ha trobat el tipus per l'element " + mod.toString());
        }
        try {
            String emoji = mod.getString("sprite");
            atributs.add(emoji);
        } catch (JSONException e) {
            System.out.println("No s'ha trobat Emoji per l'element " + mod.toString());
        }
        try {
            String colorString = mod.getString("color");
            atributs.add(colorString);
        } catch (JSONException e) {
            System.out.println("No s'ha trobat Color per l'element " + mod.toString());
        }
        try {
            String fileString = mod.getString("filename");
            File fileSprite = new File(fileString);
            if (!fileSprite.exists()) {
                System.out.println("El fitxer de sprite per " + mod.toString() + " no s'ha trobat");
            }
            atributs.add(fileString);
        } catch (JSONException e) {
            System.out.println("No s'ha trobat Arxiu per l'element" + mod.toString());
        }
        atributs.add("modded");
        spriteMap.put(type, atributs);
        GamePanel.setSpriteMap(spriteMap);
        reloadSprites();
    }

    private void addModMusic(JSONObject mod) {
        String soundType = mod.getString("type");
        String soundPath = mod.getString("filename");
        Map<String, String> newMap = soundManager.getSoundsMap();
        newMap.put(soundType, soundPath);
        soundManager.setSoundsMap(newMap);
        soundManager.stopMusic();
        // soundManager.playMusic(false);
        playMusic(false);
    }

    public void playMusic(boolean fromResource) {
        soundManager.stopMusic();
        soundManager.playMusic(fromResource);
    }

    private void addModSound(JSONObject mod) {
        String soundType = mod.getString("type");
        String soundPath = mod.getString("filename");
        Map<String, String> newMap = soundManager.getSoundsMap();
        newMap.put(soundType, soundPath);
        soundManager.setSoundsMap(newMap);
    }

    public int getIceCream() {
        return iceCream;
    }

    public int getLives() {
        return lives;
    }

    /*//////////////////////////////////////////////////////////////////////////
    ///
    //////////////////////////////////////////////////////////////////////////*/
    private TileType[][] createTileMapForAI() {
        TileType[][] tileMap = new TileType[getRows()][getCols()];

        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                tileMap[row][col] = getType(row, col);
            }
        }

        return tileMap;
    }

    public void saveLastPosition() {
        lastPositions.add(new PositionHistory(player.getRow(), player.getCol()));
        // System.out.println(lastPositions.getLast().getX() + "," + lastPositions.getLast().getY());
    }

    public PositionHistory calculatePosition() {
        if (lastPositions.size() < 2) {
            return new PositionHistory(player.getRow(), player.getCol());
        }

        PositionHistory previous = lastPositions.get(lastPositions.size() - 2);
        PositionHistory current = lastPositions.get(lastPositions.size() - 1);

        int deltaRow = current.getX() - previous.getX();
        int deltaCol = current.getY() - previous.getY();

        int nextRow = player.getRow() + deltaRow;
        int nextCol = player.getCol() + deltaCol;

        return new PositionHistory(nextRow, nextCol);
    }

    private PositionHistory getAmbushingTarget() {
        PositionHistory aimPosition = calculatePosition();

        int pRow = player.getRow();
        int pCol = player.getCol();

        if (player.getState() == fallingState) {

            int targetRow = pRow;

            while (!isOutOfBounds(targetRow, pCol) && shouldDropAt(targetRow, pCol)) {
                targetRow++;
            }

            return new PositionHistory(targetRow, pCol);
        }

        if (isOutOfBounds(aimPosition.getX(), aimPosition.getY())) {
            return new PositionHistory(pRow, pCol);
        }

        return aimPosition;
    }

    private boolean shouldDropAt(int row, int col) {
        return !isSolid(row + 1, col)
                && !isRail(row, col)
                && !isStair(row, col)
                && !isStair(row + 1, col)
                && !isEnemy(row + 1, col);
    }

    // **** //
    private static class EasyNode implements Serializable {

        int row;
        int col;
        int distance;

        EasyNode(int row, int col, int distance) {
            this.row = row;
            this.col = col;
            this.distance = distance;
        }
    }

    // **** //
    private PositionHistory canReachEasy(int enemyRow, int enemyCol, int pRow, int pCol) {
        final int MAX_EASY_DISTANCE = 2;

        if (isOutOfBounds(enemyRow, enemyCol) || isOutOfBounds(pRow, pCol)) {
            return null;
        }

        int manhattanDistance = Math.abs(enemyRow - pRow) + Math.abs(enemyCol - pCol);

        /*
         * Si el jugador està massa lluny, no volem anar directe.
         * En aquest cas l'AmbushingEnemy continuarà fent ambush.
         */
        if (manhattanDistance > MAX_EASY_DISTANCE) {
            return null;
        }

        boolean[][] visited = new boolean[getRows()][getCols()];
        Queue<EasyNode> queue = new LinkedList<>();

        visited[enemyRow][enemyCol] = true;
        queue.add(new EasyNode(enemyRow, enemyCol, 0));

        while (!queue.isEmpty()) {
            EasyNode current = queue.poll();

            if (current.row == pRow && current.col == pCol) {
                return new PositionHistory(pRow, pCol);
            }

            if (current.distance >= MAX_EASY_DISTANCE) {
                continue;
            }

            tryAddEasyPosition(queue, visited, current, Direction.UP, pRow, pCol);
            tryAddEasyPosition(queue, visited, current, Direction.DOWN, pRow, pCol);
            tryAddEasyPosition(queue, visited, current, Direction.LEFT, pRow, pCol);
            tryAddEasyPosition(queue, visited, current, Direction.RIGHT, pRow, pCol);
        }

        return null;
    }

    // **** //
    private void tryAddEasyPosition(
            Queue<EasyNode> queue,
            boolean[][] visited,
            EasyNode current,
            Direction direction,
            int targetRow,
            int targetCol
    ) {
        int nextRow = current.row + direction.getDr();
        int nextCol = current.col + direction.getDc();

        if (isOutOfBounds(nextRow, nextCol)) {
            return;
        }

        if (visited[nextRow][nextCol]) {
            return;
        }

        if (!canEnemyMoveEasyFromTo(
                current.row,
                current.col,
                nextRow,
                nextCol,
                direction,
                targetRow,
                targetCol
        )) {
            return;
        }

        visited[nextRow][nextCol] = true;
        queue.add(new EasyNode(nextRow, nextCol, current.distance + 1));
    }

    // **** //
    private boolean canEnemyMoveEasyFromTo(
            int row,
            int col,
            int nextRow,
            int nextCol,
            Direction direction,
            int targetRow,
            int targetCol
    ) {
        if (isOutOfBounds(nextRow, nextCol)) {
            return false;
        }

        if (isSolid(nextRow, nextCol)) {
            return false;
        }

        /*
         * Si hi ha un enemic a la casella següent, bloqueja.
         * Però si la casella següent és la del jugador, ho permetem.
         */
        if (isEnemy(nextRow, nextCol)
                && !(nextRow == targetRow && nextCol == targetCol)) {
            return false;
        }

        /*
         * Si està caient, només pot baixar.
         */
        if (shouldEnemyDrop(row, col)) {
            return direction == Direction.DOWN;
        }

        /*
         * Pujar:
         * només si està a una escala o si entra dins una escala.
         */
        if (direction == Direction.UP) {
            return isStair(row, col) || isStair(nextRow, nextCol);
        }

        /*
         * Baixar:
         * pot baixar si està a escala, si entra a escala,
         * o si no té terra davall i per tant cau.
         */
        if (direction == Direction.DOWN) {
            return isStair(row, col)
                    || isStair(nextRow, nextCol)
                    || !hasGroundBelowForEnemy(row, col);
        }

        /*
         * Esquerra/dreta:
         * pot moure si té suport, si va cap a una casella amb suport,
         * o si està en rail/escala.
         */
        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            return hasGroundBelowForEnemy(row, col)
                    || hasGroundBelowForEnemy(nextRow, nextCol)
                    || isRail(row, col)
                    || isRail(nextRow, nextCol)
                    || isStair(row, col)
                    || isStair(nextRow, nextCol);
        }

        return false;
    }

    // **** //
    private boolean hasGroundBelowForEnemy(int row, int col) {
        int rowBelow = row + 1;

        if (isOutOfBounds(rowBelow, col)) {
            return true;
        }

        if (isSolid(rowBelow, col)) {
            return true;
        }

        if (isMolten(rowBelow, col)) {
            return true;
        }

        return isRail(row, col) || isStair(row, col);
    }
    // **** //

    private void moveIceCreamEnemy(IceCreamEnemy enemy) {
        /*
     * Si encara no té gelat, primer mira si està damunt un gelat.
         */
        if (!enemy.hasIceCream()) {
            collectIceCreamForEnemy(enemy);
        }

        /*
     * Si ja té gelat, fuig del jugador.
         */
        if (enemy.hasIceCream()) {
            moveEnemyAwayFromPlayer(enemy);
            return;
        }

        /*
     * Si encara no té gelat, cerca el gelat més proper.
         */
        moveEnemyToNearestIceCream(enemy);
    }

// **** //
    private void collectIceCreamForEnemy(IceCreamEnemy enemy) {
        int row = enemy.getRow();
        int col = enemy.getCol();

        if (getType(row, col) == TileType.ICECREAM) {
            blocks[row][col] = new Block(TileType.BLANK);
            enemy.collectIceCream();
        }
    }

// **** //
    private void moveEnemyToNearestIceCream(IceCreamEnemy enemy) {
        Direction bestDirection = null;
        int bestDistance = Integer.MAX_VALUE;

        int enemyRow = enemy.getRow();
        int enemyCol = enemy.getCol();

        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {

                if (getType(row, col) != TileType.ICECREAM) {
                    continue;
                }

                Direction direction = buscador.getShortestDirection(
                        createPathMapForAI(enemy),
                        createTileMapForAI(),
                        enemyRow,
                        enemyCol,
                        row,
                        col
                );

                if (direction == null) {
                    continue;
                }

                int distance = Math.abs(enemyRow - row) + Math.abs(enemyCol - col);

                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestDirection = direction;
                }
            }
        }

        if (bestDirection == null) {
            return;
        }

        int nextRow = enemy.getRow() + bestDirection.getDr();
        int nextCol = enemy.getCol() + bestDirection.getDc();

        if (isEnemy(nextRow, nextCol)) {
            return;
        }

        if (canMoveTo(nextRow, nextCol) || isMolten(nextRow, nextCol)) {
            enemy.setPosition(nextRow, nextCol);
        }

        collectIceCreamForEnemy(enemy);
    }

// **** //
    private void moveEnemyAwayFromPlayer(IceCreamEnemy enemy) {
        Direction bestDirection = null;

        int enemyRow = enemy.getRow();
        int enemyCol = enemy.getCol();

        int currentDistance = distanceToPlayer(enemyRow, enemyCol);
        int bestDistance = currentDistance;

        for (Direction direction : Direction.values()) {
            int nextRow = enemyRow + direction.getDr();
            int nextCol = enemyCol + direction.getDc();

            if (!canEnemyEscapeTo(enemyRow, enemyCol, nextRow, nextCol, direction)) {
                continue;
            }

            int distance = distanceToPlayer(nextRow, nextCol);

            if (distance > bestDistance) {
                bestDistance = distance;
                bestDirection = direction;
            }
        }

        if (bestDirection == null) {
            return;
        }

        int nextRow = enemy.getRow() + bestDirection.getDr();
        int nextCol = enemy.getCol() + bestDirection.getDc();

        enemy.setPosition(nextRow, nextCol);
    }

// **** //
    private int distanceToPlayer(int row, int col) {
        return Math.abs(row - player.getRow()) + Math.abs(col - player.getCol());
    }

// **** //
    private boolean canEnemyEscapeTo(
            int row,
            int col,
            int nextRow,
            int nextCol,
            Direction direction
    ) {
        if (isOutOfBounds(nextRow, nextCol)) {
            return false;
        }

        if (isSolid(nextRow, nextCol)) {
            return false;
        }

        if (isEnemy(nextRow, nextCol)) {
            return false;
        }

        if (direction == Direction.UP) {
            return isStair(row, col) || isStair(nextRow, nextCol);
        }

        if (direction == Direction.DOWN) {
            return isStair(row, col)
                    || isStair(nextRow, nextCol)
                    || !hasGroundBelowForEnemy(row, col);
        }

        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            return hasGroundBelowForEnemy(row, col)
                    || hasGroundBelowForEnemy(nextRow, nextCol)
                    || isRail(row, col)
                    || isRail(nextRow, nextCol)
                    || isStair(row, col)
                    || isStair(nextRow, nextCol);
        }

        return false;
    }

// **** //
    private void dropIceCreamIfNeeded(Enemy enemy) {
        if (!(enemy instanceof IceCreamEnemy iceCreamEnemy)) {
            return;
        }

        if (!iceCreamEnemy.hasIceCream()) {
            return;
        }

        int row = iceCreamEnemy.getRow();
        int col = iceCreamEnemy.getCol();

        /*
     * Si mor dins una casella on es pot deixar objecte,
     * deixam un gelat a la posició on ha mort.
         */
        if (!isOutOfBounds(row, col)
                && (isBlank(row, col) || isMolten(row, col))) {
            blocks[row][col] = new Block(TileType.ICECREAM);
            blocks[row][col].setPrintables();
            iceCreamEnemy.dropIceCream();
        }
    }

}
