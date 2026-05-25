/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

/**
 *
 * @author loren
 */
import java.util.ArrayList;
import java.util.List;

/*
 * GameState conté l'estat i la lògica del joc.
 *
 * Responsabilitats:
 * - Guardar el mapa.
 * - Guardar el jugador.
 * - Guardar els enemics.
 * - Moure el jugador.
 * - Moure els enemics.
 * - Comprovar col·lisions.
 *
 * Aquesta classe NO dibuixa res.
 */
public class GameState {

    private final List<BrokenBlock> brokenBlocks = new ArrayList<>();
    private final TileType[][] map;
    private final Player player;
    private final List<Enemy> enemies;

    /*
     * Guardem les posicions inicials per poder reiniciar
     * quan l'enemic atrapa el jugador.
     */
    private final int startPlayerRow;
    private final int startPlayerCol;

    public GameState() {

        /*
         * Mapa del nivell.
         *
         * # = paret
         * . = terra
         * G = or
         * P = jugador
         * E = enemic
         */
        String[] level = {};
        DefaultMaps maps = new DefaultMaps();
        int amountOfMaps = maps.getAmountOfMaps();
        if(amountOfMaps >= 1){
            level = maps.getMap(0);
        }
        

        map = new TileType[level.length][level[0].length()];
        enemies = new ArrayList<>();

        Player foundPlayer = null;

        /*
         * Valors temporals per recordar on comença el jugador.
         */
        int tempStartRow = 1;
        int tempStartCol = 1;

        /*
         * Convertim el mapa de text a TileType[][].
         */
        for (int row = 0; row < level.length; row++) {
            for (int col = 0; col < level[row].length(); col++) {

                char symbol = level[row].charAt(col);

                switch (symbol) {
                    case '#':
                        map[row][col] = TileType.WALL;
                        break;
                    case 'G':
                        map[row][col] = TileType.ICECREAM;
                        break;
                    case '.':
                        map[row][col] = TileType.ICE;
                        break;
                    case 'H':
                        map[row][col] = TileType.STAIR;
                        break;
                    case '-':
                        map[row][col] = TileType.RAIL;
                        break;
                    default:
                        map[row][col] = TileType.BLANK;
                        break;
                }

                /*
                 * Si trobem el jugador, el creem.
                 * La casella on hi havia P passa a ser terra.
                 */
                if (symbol == 'P') {
                    foundPlayer = new Player(row, col);
                    tempStartRow = row;
                    tempStartCol = col;
                }

                /*
                 * Si trobem un enemic, l'afegim a la llista.
                 * La casella on hi havia E passa a ser terra.
                 */
                if (symbol == 'E') {
                    enemies.add(new Enemy(row, col, 1, 1));
                }
            }
        }

        player = foundPlayer;

        startPlayerRow = tempStartRow;
        startPlayerCol = tempStartCol;
    }

    /*
     * Aquest és el mètode principal de la lògica per torns.
     *
     * Cada vegada que el jugador prem una fletxa:
     * 1. Es mou el jugador.
     * 2. Es mouen els enemics.
     * 3. Es comproven col·lisions.
     */
    public void takeTurn(Direction direction) {

        movePlayer(direction);
        collectIcecream();
        moveEnemies();
        updateBrokenBlocks();
        checkCollisions();

    }

    public void takeTurn() {
        collectIcecream();
        moveEnemies();
        updateBrokenBlocks();
        checkCollisions();

    }

    /*
     * Mou el jugador una casella en la direcció indicada.
     */
    private void movePlayer(Direction direction) {
        int actualRow = player.getRow();
        int actualCol = player.getCol();

        int nextRow = actualRow + direction.getDr();
        int nextCol = actualCol + direction.getDc();

        boolean iceUnderneath = isIce(actualRow + 1, actualCol);
        boolean onRail = isRail(actualRow, actualCol); // maquina d'estats
        boolean isClimbing = isStair(actualRow, actualCol); // maquina d'estats
        boolean stairUnderneath = isStair(actualRow + 1, actualCol);
        boolean enemyUnderneath = isEnemy(actualRow + 1, actualCol);

        // Si vol pujar, només pot fer-ho si està damunt una escala
        if (direction == Direction.UP) {
            if (canMoveTo(nextRow, nextCol) && isClimbing) {
                player.setPosition(nextRow, nextCol);
            }
            return;
        }
        if (direction == Direction.DOWN) {
            if (canMoveTo(nextRow, nextCol) && (isClimbing || !enemyUnderneath || stairUnderneath)) {
                player.setPosition(nextRow, nextCol);
            }
            return;
        }

        // Per a la resta de direccions, aplica les regles normals
        if (canMoveTo(nextRow, nextCol)) {
            player.setPosition(nextRow, nextCol);
        }
    }

    /*
       * Fa accions
     */
    public void breakDownLeft() {
        breakBlock(player.getRow() + 1, player.getCol() - 1);
    }

    public void breakDownRight() {
        breakBlock(player.getRow() + 1, player.getCol() + 1);
    }

    private void breakBlock(int row, int col) {
        if (isOutOfBounds(row, col)) {
            return;
        }
        if (map[row][col] == TileType.ICE) {
            map[row][col] = TileType.MOLTEN;
            brokenBlocks.add(new BrokenBlock(row, col, 5));
        }

    }

    /*
     * Mou tots els enemics.
     */
    private void moveEnemies() {
        for (Enemy enemy : enemies) {

            if (enemy.getIsDead()) {
                enemy.subtractTimeToRevive(1);
                if (enemy.getTimeToRevive() <= 0) {
                    enemy.revive();
                }
            } else {
                if (!isFos(enemy.getRow(), enemy.getCol())) {
                    moveEnemy(enemy);
                }

            }

        }
    }

    /*
     * Mou un enemic cap al jugador de manera molt senzilla.
     *
     * Primer intenta acostar-se en vertical.
     * Si ja està a la mateixa fila, intenta acostar-se en horitzontal.
     */
    private void moveEnemy(Enemy enemy) {
        int row = enemy.getRow();
        int col = enemy.getCol();
        int dr = 0;
        int dc = 0;
        if (shouldDie(row, col) && !enemy.getIsDead()) {
            enemy.die();
            enemy.setTimeToRevive(7);
            return;
        }
        if (shouldDrop(row, col)) {
            enemy.setPosition(row + 1, col);
            return;
        }
        if (enemy.getRow() < player.getRow()) {
            dr = 1;
        } else if (enemy.getRow() > player.getRow()) {
            //amunt, de moment desactivat, falta fer comprovacio de si esta a una escala
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

    /*
     * Comprova si una posició és vàlida per moure's.
     */
    private boolean canMoveTo(int row, int col) {
        return !isOutOfBounds(row, col) && !isWall(row, col) && !isIce(row, col);
    }

    /*
     * Comprova si una posició surt fora del mapa.
     */
    private boolean isOutOfBounds(int row, int col) {
        return row < 0
                || row >= getRows()
                || col < 0
                || col >= getCols();
    }

    /*
     * Comprova si una casella és paret.
     */
    private boolean isWall(int row, int col) {
        return map[row][col] == TileType.WALL;
    }

    private boolean isIce(int row, int col) {
        return map[row][col] == TileType.ICE;
    }

    private boolean isRail(int row, int col) {
        return map[row][col] == TileType.RAIL;
    }

    private boolean isStair(int row, int col) {
        return map[row][col] == TileType.STAIR;
    }

    private boolean isFos(int row, int col) {
        return map[row][col] == TileType.MOLTEN;
    }

    private boolean isEnemy(int row, int col) {
        for (Enemy enemy : enemies) {
            if (enemy.getRow() == row && enemy.getCol() == col) {
                return true;
            }

        }
        return false;
    }

    /*
     * Si el jugador trepitja una casella amb gelat,
     * la convertim en res.
     */
    private void collectIcecream() {
        int row = player.getRow();
        int col = player.getCol();

        if (map[row][col] == TileType.ICECREAM) {
            map[row][col] = TileType.BLANK;
        }
    }

    /*
     * Comprova si algun enemic està a la mateixa casella que el jugador.
     */
    private void checkCollisions() {
        int actualRow = player.getRow();
        int actualCol = player.getCol();
        for (Enemy enemy : enemies) {
            if ((enemy.getRow() == player.getRow()
                    && enemy.getCol() == player.getCol()) || isIce(actualRow, actualCol)) {

                resetPositions();
            }
        }
    }

    /*
     * Reinicia el jugador i els enemics.
     * De moment ho fem simple.
     */
    private void resetPositions() {
        player.setPosition(startPlayerRow, startPlayerCol);

        if (!enemies.isEmpty()) {
            for (Enemy enemy : enemies) {
                enemy.moveToOriginalRow();
            }
        }
    }

    /*
     * Getters perquè el GamePanel pugui consultar informació.
     */
    public TileType getTile(int row, int col) {
        return map[row][col];
    }

    public int getRows() {
        return map.length;
    }

    public int getCols() {
        return map[0].length;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    private static class BrokenBlock {

        int row;
        int col;
        int turnsLeft;

        BrokenBlock(int row, int col, int turnsLeft) {
            this.row = row;
            this.col = col;
            this.turnsLeft = turnsLeft;
        }
    }

    //actualitzar blocs
    private void updateBrokenBlocks() {
        for (int posicio = brokenBlocks.size() - 1; posicio >= 0; posicio--) {
            BrokenBlock block = brokenBlocks.get(posicio);

            block.turnsLeft--;

            if (block.turnsLeft <= 0) {
                map[block.row][block.col] = TileType.ICE;
                brokenBlocks.remove(posicio);
            }
        }
    }

    //mira si caus
    public boolean shouldDrop() {
        int actualRow = player.getRow();
        int actualCol = player.getCol();

        boolean hiHaGelDavall = isIce(actualRow + 1, actualCol);
        boolean estaDamuntPasarela = isRail(actualRow, actualCol);
        boolean estaEnLaEscala = isStair(actualRow, actualCol);
        boolean hiHaEscalaDavall = isStair(actualRow + 1, actualCol);
        boolean hiHaParetDavall = isWall(actualRow + 1, actualCol);
        boolean hiHaEnemicDavall = isEnemy(actualRow + 1, actualCol);

        return !hiHaGelDavall && !estaDamuntPasarela && !estaEnLaEscala && !hiHaEscalaDavall && !hiHaParetDavall && !hiHaEnemicDavall;
    }

    private boolean shouldDrop(int row, int col) {
        return !isIce(row + 1, col)
                && !isWall(row + 1, col)
                && !isRail(row, col)
                && !isStair(row, col)
                && !isStair(row + 1, col)
                && !isEnemy(row + 1, col)
                && isFos(row + 1, col)
                && !isFos(row, col);
    }

    private boolean shouldDie(int row, int col) {
        return isIce(row, col);
    }

    public void applyGravity() {
        int actualRow = player.getRow();
        int actualCol = player.getCol();

        int nextRow = actualRow + 1;
        int nextCol = actualCol;
        if (canMoveTo(nextRow, nextCol)) {
            player.setPosition(nextRow, nextCol);
            collectIcecream();
        }

    }
}
