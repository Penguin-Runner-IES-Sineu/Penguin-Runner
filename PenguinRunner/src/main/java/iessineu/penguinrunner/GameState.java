/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

/**
 *
 * @author loren
 */
import AI.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONObject;

import iessineu.penguinrunner.Blocks.Block;
import iessineu.penguinrunner.Blocks.TileType;
import iessineu.penguinrunner.Entity.Enemy;
import iessineu.penguinrunner.Entity.EnemyStarAi;
import iessineu.penguinrunner.Entity.GameMap;
import iessineu.penguinrunner.Entity.Player;
import iessineu.penguinrunner.Movement.Direction;
import iessineu.penguinrunner.States.ClimbingState;
import iessineu.penguinrunner.States.FallingState;
import iessineu.penguinrunner.States.PlayerState;
import iessineu.penguinrunner.States.RailState;
import iessineu.penguinrunner.States.WalkingState;

public class GameState implements Serializable {

    private List<BrokenBlock> brokenBlocks;
    private List<Block> stones;
    // private String rutaMapes = "resources/maps.json";
    private final String rutaMapes = "resources/maps.json";
    private int nivellActual = 0;
    // private long lastTurn;
    private final AI buscador = new AI();
    private final List<GameMap> mapList = llegirMapes(rutaMapes);
    private GameMap mapObject = mapList.get(2);
    private Player player;
    private List<Enemy> enemies;
    private int iceCream = 0;
    private int startPlayerRow;
    private int startPlayerCol;

    private final SoundManager soundManager = new SoundManager();
    private final PlayerState walkingState = new WalkingState();
    private final PlayerState climbingState = new ClimbingState();
    private final PlayerState railState = new RailState();
    private final PlayerState fallingState = new FallingState();
    private final GameFrame gameFrame;

    private static final int NOT_VISITED = -1;
    private static final int BLOCKED = -2;

    public GameState(GameFrame frame) {
        this.gameFrame = frame;
    }

    private Block[][] blocks = loadMap();

    public Block[][] loadMap() {
        String[] level = mapObject.getMap();
        blocks = new Block[level.length][level[0].length()];
        brokenBlocks = new ArrayList<>();
        stones = new ArrayList<>();
        enemies = new ArrayList();
        player = null;
        startPlayerRow = 0;
        startPlayerCol = 0;
        iceCream = 0;

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
                    case 'G' -> {
                        blocks[row][col] = new Block(TileType.ICECREAM);
                        iceCream++;
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
                    case 'P' -> {
                        player = new Player(row, col);
                        startPlayerRow = row;
                        startPlayerCol = col;
                        blocks[row][col] = new Block(TileType.BLANK);
                    }
                    case 'E' -> {
                        enemies.add(new Enemy(row, col, 1, 1));
                        blocks[row][col] = new Block(TileType.BLANK);
                    }
                    case 'A' -> {
                        enemies.add(new EnemyStarAi(row, col, 1, 1));
                        blocks[row][col] = new Block(TileType.BLANK);
                    }
                    default -> {
                        blocks[row][col] = new Block(TileType.BLANK);
                    }
                }
            }
        }
        updatePlayerState();

        return blocks;
    }

    private int[][] createEmptyPathMap() {
        int[][] pathMap = new int[getRows()][getCols()];

        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                if (isSolid(row, col)) {
                    pathMap[row][col] = BLOCKED;
                } else {
                    pathMap[row][col] = NOT_VISITED;
                }
            }
        }

        return pathMap;
    }

    public void reloadSprites() {
        mapObject = mapList.get(nivellActual);
        Block[][] mapa = getBlocks();
        List<Block> newStoneList = new ArrayList();
        Block[][] mapaNou = new Block[mapa.length][mapa[0].length];
        for (int row = 0; row < mapa.length; row++) {
            for (int col = 0; col < mapa[row].length; col++) {
                Block blocAntic = mapa[row][col];
                if (blocAntic == null) {
                    System.out.println("Hi ha blocs nuls?");
                    mapaNou[row][col] = new Block(TileType.BLANK);
                } else {
                    mapaNou[row][col] = new Block(blocAntic.getType());
                    mapaNou[row][col] = new Block(blocAntic.getType());
                    if (blocAntic.getType() == TileType.STONE) {
                        newStoneList.add(mapaNou[row][col]);
                    }
                }
            }
        }
        List<Enemy> newEnemyList = new ArrayList();
        for (Enemy enemy : getEnemies()) {
            enemy = new Enemy(enemy.getRow(), enemy.getCol(), enemy.getRespawnCol(), enemy.getRespawnRow());
            newEnemyList.add(enemy);
        }
        enemies = newEnemyList;
        player = new Player(player.getRow(), player.getCol());
        stones = newStoneList;
        blocks = mapaNou;
        updatePlayerState();
    }

    /*
     * TORNS
     */
    public void takeTurn(Direction direction) {
        // long delta = System.currentTimeMillis() - lastTurn;
        if (player.getState() == fallingState) {
            // if (delta > 50) {
            // lastTurn = System.currentTimeMillis();
            movePlayerDownOne();
            updatePlayerState();
            finishTurn();
            takeTurn();
            // }
        } else if (direction != null) {
            player.getState().handleInput(this, direction);
            updatePlayerState();
            finishTurn();
            if (player.getState() == fallingState) {
                // lastTurn = System.currentTimeMillis();
                takeTurn();
            }
        }

    }

    public void takeTurn() {
        takeTurn(null);
    }

    private void finishTurn() {
        collectIcecream();
        moveBlocks();
        moveEnemies();
        updateBrokenBlocks();
        checkCollisions();
        updatePlayerState();

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

        return !isSolid(row + 1, col)
                && !isRail(row, col)
                && !isStair(row, col)
                && !isStair(row + 1, col)
                && !isEnemy(row + 1, col);
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

            // blockToMove.setPosition(row, col + dc);
            col -= dc;
        }

        return true;
    }

    private void moveBlocks() {
        for (int row = blocks.length - 1; row >= 0; row--) {
            for (int col = 0; col < blocks[row].length; col++) {
                if (blocks[row][col].getType() == TileType.STONE) {
                    int nextRow = row + 1;
                    int nextCol = col;

                    if (isOutOfBounds(nextRow, nextCol)) {
                        continue;
                    }

                    if (!isBlank(nextRow, nextCol)) {
                        continue;
                    }

                    if (player.getRow() == nextRow && player.getCol() == nextCol) {
                        continue;
                    }

                    if (isEnemy(nextRow, nextCol)) {
                        continue;
                    }

                    blocks[nextRow][nextCol] = blocks[row][col];
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
            finishTurn();
        }
    }

    public void breakDownRight() {
        if (canMoveTo(player.getRow(), player.getCol() + 1)) {
            breakBlock(player.getRow() + 1, player.getCol() + 1);
            finishTurn();
        }
    }

    private void breakBlock(int row, int col) {
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
                    enemy.revive();
                }

                continue;
            }

            if (!isMolten(enemy.getRow(), enemy.getCol())) {
                moveEnemy(enemy);
            }
        }
    }

    private void moveEnemy(Enemy enemy) {
        int row = enemy.getRow();
        int col = enemy.getCol();

        if (shouldDie(row, col)) {
            enemy.die();
            enemy.setTimeToRevive(7);
            return;
        }

        if (shouldEnemyDrop(row, col)) {
            enemy.setPosition(row + 1, col);
            return;
        }

        if (enemy instanceof EnemyStarAi enemyStarAi) {
            Direction direction = buscador.getShortestDirection(
                    createEmptyPathMap(),
                    createTileMapForAI(),
                    enemyStarAi.getRow(),
                    enemyStarAi.getCol(),
                    player.getRow(),
                    player.getCol()
            );

            if (direction == null) {
                return;
            }

            int nextRow = enemyStarAi.getRow() + direction.getDr();
            int nextCol = enemyStarAi.getCol() + direction.getDc();

            if (canMoveTo(nextRow, nextCol)) {
                enemyStarAi.setPosition(nextRow, nextCol);
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

        if (canMoveTo(nextRow, nextCol)) {
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
    private void collectIcecream() {
        int row = player.getRow();
        int col = player.getCol();

        Block block = blocks[row][col];

        if (block != null && block.isCollectable()) {
            blocks[row][col] = new Block(TileType.BLANK);
            soundManager.playSound("resources/nyam.wav");
            player.addIceCream();

            System.out.println("Gelat: " + player.geticeCream());
        }
    }

    private void checkCollisions() {
        int playerRow = player.getRow();
        int playerCol = player.getCol();

        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && enemy.getRow() == playerRow && enemy.getCol() == playerCol) {
                loadMap();
                return;
            }
        }

        if (isIce(playerRow, playerCol)) {
            loadMap();
        }
    }

    /*
     * CONSULTES DE BLOCS
     */
    private boolean canMoveTo(int row, int col) {
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
            nivellActual++;
            mapObject = mapList.get(nivellActual);
            loadMap();
        }
    }

    public boolean checkObjective() {
        return player.geticeCream() >= 2;
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

        JSONArray maps = new JSONArray(llegirJSON(rutaMapes));
        List<GameMap> llista = new ArrayList();

        for (int i = 0; i < maps.length(); i++) {
            JSONObject obj = maps.getJSONObject(i);
            JSONArray jsonView = obj.getJSONArray("view");

            String[] view = new String[jsonView.length()];

            for (int j = 0; j < view.length; j++) {
                view[j] = jsonView.getString(j);
            }

            llista.add(new GameMap(view));
        }
        return llista;
    }

    public String llegirJSON(String rutaArxiu) {
        String jsonString = "";
        try {
            BufferedReader fitxer = new BufferedReader(new FileReader(rutaArxiu));
            try {
                String line;

                while ((line = fitxer.readLine()) != null) {
                    jsonString += line;
                }

                fitxer.close();

            } catch (IOException ex) {
                System.out.println("Problema d'entrada i sortida");
            }

        } catch (FileNotFoundException ex) {
            System.out.println("L'arxiu no s'ha trobat!");
        }
        return jsonString;
    }

    public int getIceCream() {
        return iceCream;
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
}
