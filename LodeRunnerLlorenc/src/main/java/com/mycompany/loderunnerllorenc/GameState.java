/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.loderunnerllorenc;

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
        String[] level = {
            "####################",
            "#pnnnnnnnngnnnnnnnn#",
            "#n.......nnnn....nn#",
            "#nnnnnn.nnEnnnnn.nn#",
            "#nn.Gnn.n---ngnn.nn#",
            "#.....n........n.nn#",
            "#nnnnnn.nnnnnnnn.nn#",
            "#nnEnnn.nnn....nnnn#",
            "#nnnnnn.nnnnngnnnnn#",
            "#nn............Hnnn#",
            "#nnnnnnnnnnn.nnHnnn#",
            "#PnnnnnnnnnnnnnHnnn#",
            "#..................#",
            "####################"
        };

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

                if (symbol == '#') {
                    map[row][col] = TileType.WALL;
                } else if (symbol == 'G') {
                    map[row][col] = TileType.GELAT;
                } else if (symbol == '.') {
                    map[row][col] = TileType.GEL;
                } else if (symbol == 'H') {
                    map[row][col] = TileType.ESCALA;
                } else if (symbol == '-') {
                    map[row][col] = TileType.PASARELA;
                } else {
                    map[row][col] = TileType.RES;
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
                    enemies.add(new Enemy(row, col));
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
        collectGold();
        moveEnemies();
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

        boolean hiHaGelDavall = isGel(actualRow + 1, actualCol);
        boolean estaDamuntPasarela = isPasarela(actualRow, actualCol);
        boolean estaEnLaEscala = isEscala(actualRow, actualCol);
        boolean hiHaEscalaDavall = isEscala(actualRow+1, actualCol);

        // Si vol pujar, només pot fer-ho si està damunt una escala
        if (direction == Direction.UP) {
            if (canMoveTo(nextRow, nextCol) && estaEnLaEscala) {
                player.setPosition(nextRow, nextCol);
            }
            return;
        }

        // Per a la resta de direccions, aplica les regles normals
        if (canMoveTo(nextRow, nextCol)
                && (hiHaGelDavall || estaDamuntPasarela || estaEnLaEscala|| hiHaEscalaDavall)) {
            player.setPosition(nextRow, nextCol);
        }
    }

    /*
     * Mou tots els enemics.
     */
    private void moveEnemies() {
        for (Enemy enemy : enemies) {
            moveEnemy(enemy);
        }
    }

    /*
     * Mou un enemic cap al jugador de manera molt senzilla.
     *
     * Primer intenta acostar-se en vertical.
     * Si ja està a la mateixa fila, intenta acostar-se en horitzontal.
     */
    private void moveEnemy(Enemy enemy) {
        int dr = 0;
        int dc = 0;

        if (enemy.getRow() < player.getRow()) {
            dr = 1;
        } else if (enemy.getRow() > player.getRow()) {
            dr = -1;
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
        return !isOutOfBounds(row, col) && !isWall(row, col) && !isGel(row, col);
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

    private boolean isGel(int row, int col) {
        return map[row][col] == TileType.GEL;
    }

    private boolean isPasarela(int row, int col) {
        return map[row][col] == TileType.PASARELA;
    }

    private boolean isEscala(int row, int col) {
        return map[row][col] == TileType.ESCALA;
    }

    /*
     * Si el jugador trepitja una casella amb or,
     * la convertim en terra.
     */
    private void collectGold() {
        int row = player.getRow();
        int col = player.getCol();

        if (map[row][col] == TileType.GELAT) {
            map[row][col] = TileType.RES;
        }
    }

    /*
     * Comprova si algun enemic està a la mateixa casella que el jugador.
     */
    private void checkCollisions() {
        for (Enemy enemy : enemies) {
            if (enemy.getRow() == player.getRow()
                    && enemy.getCol() == player.getCol()) {

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
            enemies.get(0).setPosition(4, 7);
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
}
