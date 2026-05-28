/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

/**
 *
 * @author loren
 */
import iessineu.penguinrunner.Blocks.Block;
import iessineu.penguinrunner.Blocks.Door;
import iessineu.penguinrunner.Blocks.Ice;
import iessineu.penguinrunner.Blocks.IceCream;
import iessineu.penguinrunner.Blocks.Ladder;
import iessineu.penguinrunner.Blocks.Molten;
import iessineu.penguinrunner.Blocks.Rail;
import iessineu.penguinrunner.Blocks.Stone;
import iessineu.penguinrunner.Blocks.TileType;
import iessineu.penguinrunner.Blocks.Wall;
import iessineu.penguinrunner.Entity.Enemy;
import iessineu.penguinrunner.Entity.Map;
import iessineu.penguinrunner.Entity.Player;
import iessineu.penguinrunner.Movement.Direction;

import iessineu.penguinrunner.States.ClimbingState;
import iessineu.penguinrunner.States.FallingState;
import iessineu.penguinrunner.States.PlayerState;
import iessineu.penguinrunner.States.RailState;

import iessineu.penguinrunner.States.WalkingState;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class GameState implements Serializable {

    private final List<BrokenBlock> brokenBlocks = new ArrayList<>();
    private final List<Stone> stones = new ArrayList<>();

    private final Block[][] blocks;

    private Map mapObject;

    private final Player player;
    private final List<Enemy> enemies;

    private int iceCream = 0;

    private final int startPlayerRow;
    private final int startPlayerCol;

    private final PlayerState walkingState = new WalkingState();
    private final PlayerState climbingState = new ClimbingState();
    private final PlayerState railState = new RailState();
    private final PlayerState fallingState = new FallingState();

    public GameState() {
        List<Map> mapList = llegirMapes();
        this.mapObject = mapList.get(0);

        String[] level = mapObject.getMap();

        blocks = new Block[level.length][level[0].length()];
        enemies = new ArrayList<>();

        Player foundPlayer = null;

        int tempStartRow = 1;
        int tempStartCol = 1;

        for (int row = 0; row < level.length; row++) {
            for (int col = 0; col < level[row].length(); col++) {
                char symbol = level[row].charAt(col);

                switch (symbol) {
                    case '#':
                        blocks[row][col] = new Wall(row, col);
                        break;

                    case '.':
                        blocks[row][col] = new Ice(row, col);
                        break;

                    case 'G':
                        blocks[row][col] = new IceCream(row, col);
                        iceCream++;
                        break;

                    case 'H':
                        blocks[row][col] = new Ladder(row, col);
                        break;

                    case '-':
                        blocks[row][col] = new Rail(row, col);
                        break;

                    case 'D':
                        blocks[row][col] = new Door(row, col);
                        break;

                    case 'S':
                        Stone stone = new Stone(row, col);
                        blocks[row][col] = stone;
                        stones.add(stone);
                        break;

                    case 'P':
                        foundPlayer = new Player(row, col);
                        tempStartRow = row;
                        tempStartCol = col;
                        blocks[row][col] = null;
                        break;

                    case 'E':
                        enemies.add(new Enemy(row, col, 1, 1));
                        blocks[row][col] = null;
                        break;

                    default:
                        blocks[row][col] = null;
                        break;
                }
            }
        }

        player = foundPlayer;

        startPlayerRow = tempStartRow;
        startPlayerCol = tempStartCol;

        updatePlayerState();
    }

    /*
     * TORNS
     */
    public void takeTurn(Direction direction) {
        updatePlayerState();

        if (direction != null) {
            player.getState().handleInput(this, direction);
        } else if (player.getState() == fallingState) {
            player.getState().handleInput(this, null);
        }

        finishTurn();
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

            blocks[row][col] = null;
            blocks[row][col + dc] = blockToMove;

            blockToMove.setPosition(row, col + dc);

            col -= dc;
        }

        return true;
    }

    private void moveBlocks() {
        for (Stone stone : stones) {
            int row = stone.getRow();
            int col = stone.getCol();

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

            blocks[row][col] = null;
            blocks[nextRow][nextCol] = stone;

            stone.setPosition(nextRow, nextCol);
        }
    }

    /*
     * ACCIONS
     */
    public void breakDownLeft() {
        breakBlock(player.getRow() + 1, player.getCol() - 1);
        finishTurn();
    }

    public void breakDownRight() {
        breakBlock(player.getRow() + 1, player.getCol() + 1);
        finishTurn();
    }

    private void breakBlock(int row, int col) {
        if (isOutOfBounds(row, col)) {
            return;
        }

        Block block = blocks[row][col];

        if (block != null && block.isBreakable()) {
            blocks[row][col] = new Molten(row, col);
            brokenBlocks.add(new BrokenBlock(row, col, 5));
        }
    }

    private void updateBrokenBlocks() {
        for (int i = brokenBlocks.size() - 1; i >= 0; i--) {
            BrokenBlock block = brokenBlocks.get(i);

            block.turnsLeft--;

            if (block.turnsLeft <= 0) {
                blocks[block.row][block.col] = new Ice(block.row, block.col);
                brokenBlocks.remove(i);
            }
        }
    }

    /*
     * ENEMICS
     */
    private void moveEnemies() {
        for (Enemy enemy : enemies) {
            if (enemy.getIsDead()) {
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
            blocks[row][col] = null;
            player.addIceCream();

            System.out.println("Gelat: " + player.geticeCream());
        }
    }

    private void checkCollisions() {
        int playerRow = player.getRow();
        int playerCol = player.getCol();

        for (Enemy enemy : enemies) {
            if (!enemy.getIsDead()
                    && enemy.getRow() == playerRow
                    && enemy.getCol() == playerCol) {
                resetPositions();
                return;
            }
        }

        if (isIce(playerRow, playerCol)) {
            resetPositions();
        }
    }

    private void resetPositions() {
        player.setPosition(startPlayerRow, startPlayerCol);

        for (Stone stone : stones) {
            blocks[stone.getRow()][stone.getCol()] = null;

            stone.moveToOriginalPosition();

            blocks[stone.getRow()][stone.getCol()] = stone;
        }

        for (Enemy enemy : enemies) {
            enemy.moveToOriginalRow();
        }

        updatePlayerState();
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
            return null;
        }

        return blocks[row][col];
    }

    private boolean isBlank(int row, int col) {
        return !isOutOfBounds(row, col) && blocks[row][col] == null;
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

    private boolean isWall(int row, int col) {
        Block block = getBlock(row, col);

        return block != null && block.getType() == TileType.WALL;
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
            if (!enemy.getIsDead()
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
    public TileType getTile(int row, int col) {
        Block block = getBlock(row, col);

        if (block == null) {
            return TileType.BLANK;
        }

        return block.getType();
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

    public int getLevel() {
        return mapObject.getLevel();
    }

    public Block[][] getBlocks() {
        return blocks;
    }

    void interact() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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
    public List<Map> llegirMapes() {
        String jsonString = "";

        try {
            BufferedReader fitxer = new BufferedReader(new FileReader("resources/maps.json"));

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
            System.out.println("L'arxiu de mapes no s'ha trobat!");
        }

        JSONArray maps = new JSONArray(jsonString);
        List<Map> mapList = new ArrayList<>();

        for (int i = 0; i < maps.length(); i++) {
            JSONObject obj = maps.getJSONObject(i);
            JSONArray jsonView = obj.getJSONArray("view");

            String[] view = new String[jsonView.length()];

            for (int j = 0; j < view.length; j++) {
                view[j] = jsonView.getString(j);
            }

            mapList.add(new Map(obj.getInt("level"), view));
        }

        return mapList;
    }

    public int getIceCream() {
        return iceCream;
    }
}
