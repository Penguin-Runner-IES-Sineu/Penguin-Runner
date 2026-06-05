/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity.enemies;

/**
 *
 * @author loren
 */
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

import iessineu.penguinrunner.Blocks.TileType;
import iessineu.penguinrunner.Movement.Direction;

public class AI implements Serializable {

    private static final int BLOCKED = -2;

    private static class Node implements Serializable {

        int row;
        int col;

        Node(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public Direction getShortestDirection(
            int[][] pathMap,
            TileType[][] tileMap,
            int startRow,
            int startCol,
            int targetRow,
            int targetCol
    ) {
        if (pathMap == null || pathMap.length == 0 || pathMap[0].length == 0) {
            return null;
        }

        if (tileMap == null || tileMap.length == 0 || tileMap[0].length == 0) {
            return null;
        }

        if (!isInsideBounds(pathMap, startRow, startCol)) {
            return null;
        }

        if (!isInsideBounds(pathMap, targetRow, targetCol)) {
            return null;
        }

        if (isBlocked(pathMap[startRow][startCol])) {
            return null;
        }

        /*
         * Abans es feia això:
         *
         * if (isBlocked(pathMap[targetRow][targetCol])) {
         *     return null;
         * }
         *
         * Però si el jugador està darrere d'un enemic o d'un bloqueig temporal,
         * volem que l'enemic avanci fins a la casella accessible més propera.
         * Per això no retornam null directament encara que el target estigui bloquejat.
         */

        int rows = pathMap.length;
        int cols = pathMap[0].length;

        boolean[][] visited = new boolean[rows][cols];

        int[][] previousRow = new int[rows][cols];
        int[][] previousCol = new int[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                previousRow[row][col] = -1;
                previousCol[row][col] = -1;
            }
        }

        Queue<Node> queue = new LinkedList<>();

        visited[startRow][startCol] = true;
        queue.add(new Node(startRow, startCol));

        boolean foundTarget = false;

        /*
         * Guardam la millor casella accessible trobada fins ara.
         *
         * Si no podem arribar exactament al jugador, ens mourem cap a la
         * casella accessible que estigui més a prop del jugador.
         */
        int bestRow = startRow;
        int bestCol = startCol;
        int bestDistanceToTarget = distanceToTarget(startRow, startCol, targetRow, targetCol);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            int distanceToTarget = distanceToTarget(
                    current.row,
                    current.col,
                    targetRow,
                    targetCol
            );

            /*
             * Si aquesta casella està més a prop del jugador que la millor
             * que havíem trobat, la guardam.
             */
            if (distanceToTarget < bestDistanceToTarget) {
                bestDistanceToTarget = distanceToTarget;
                bestRow = current.row;
                bestCol = current.col;
            }

            if (current.row == targetRow && current.col == targetCol) {
                foundTarget = true;
                break;
            }

            tryMove(
                    pathMap,
                    tileMap,
                    queue,
                    visited,
                    previousRow,
                    previousCol,
                    current.row,
                    current.col,
                    Direction.LEFT
            );

            tryMove(
                    pathMap,
                    tileMap,
                    queue,
                    visited,
                    previousRow,
                    previousCol,
                    current.row,
                    current.col,
                    Direction.UP
            );

            tryMove(
                    pathMap,
                    tileMap,
                    queue,
                    visited,
                    previousRow,
                    previousCol,
                    current.row,
                    current.col,
                    Direction.RIGHT
            );

            tryMove(
                    pathMap,
                    tileMap,
                    queue,
                    visited,
                    previousRow,
                    previousCol,
                    current.row,
                    current.col,
                    Direction.DOWN
            );
        }

        /*
         * Si no hem arribat exactament al jugador, però hem trobat una casella
         * accessible més propera que la inicial, anam cap a aquella.
         *
         * Això fa que si un enemic bloqueja el camí, l'altre avanci fins quedar
         * darrere seu en lloc de quedar-se quiet.
         */
        if (!foundTarget) {
            if (bestRow == startRow && bestCol == startCol) {
                return null;
            }

            return rebuildFirstDirection(
                    startRow,
                    startCol,
                    bestRow,
                    bestCol,
                    previousRow,
                    previousCol
            );
        }

        return rebuildFirstDirection(
                startRow,
                startCol,
                targetRow,
                targetCol,
                previousRow,
                previousCol
        );
    }

    private void tryMove(
            int[][] pathMap,
            TileType[][] tileMap,
            Queue<Node> queue,
            boolean[][] visited,
            int[][] previousRow,
            int[][] previousCol,
            int currentRow,
            int currentCol,
            Direction direction
    ) {
        int nextRow = currentRow + direction.getDr();
        int nextCol = currentCol + direction.getDc();

        if (!isInsideBounds(pathMap, nextRow, nextCol)) {
            return;
        }

        if (visited[nextRow][nextCol]) {
            return;
        }

        if (!canMove(
                pathMap,
                tileMap,
                currentRow,
                currentCol,
                nextRow,
                nextCol,
                direction
        )) {
            return;
        }

        visited[nextRow][nextCol] = true;

        previousRow[nextRow][nextCol] = currentRow;
        previousCol[nextRow][nextCol] = currentCol;

        queue.add(new Node(nextRow, nextCol));
    }

    private boolean canMove(
            int[][] pathMap,
            TileType[][] tileMap,
            int currentRow,
            int currentCol,
            int nextRow,
            int nextCol,
            Direction direction
    ) {
        if (!isInsideBounds(pathMap, nextRow, nextCol)) {
            return false;
        }

        if (isBlocked(pathMap[nextRow][nextCol])) {
            return false;
        }

        TileType currentTile = tileMap[currentRow][currentCol];
        TileType nextTile = tileMap[nextRow][nextCol];

        /*
     * MOLTEN is walkable for the AI.
     * Do NOT reject nextTile == TileType.MOLTEN here.
         */

 /*
     * Moving up:
     * The enemy can only go up if it is currently on a stair
     * or if the next tile is a stair.
         */
        if (direction == Direction.UP) {
            return isStair(currentTile) || isStair(nextTile);
        }

        /*
     * Moving down:
     * The enemy can go down if:
     * - it is on a stair
     * - the next tile is a stair
     * - or there is no ground below, so it is falling
         */
        if (direction == Direction.DOWN) {
            return isStair(currentTile)
                    || isStair(nextTile)
                    || !hasGroundBelow(pathMap, tileMap, currentRow, currentCol);
        }

        /*
     * Moving left or right:
     *
     * The enemy can move sideways if:
     * - the next position has ground below
     * - or the current position has ground below
     *   This allows the enemy to walk off an edge and fall afterwards.
     * - or it is on a rail
     * - or it is moving onto a rail
     * - or it is on a stair
     * - or it is moving onto a stair
         */
        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            return hasGroundBelow(pathMap, tileMap, nextRow, nextCol)
                    || hasGroundBelow(pathMap, tileMap, currentRow, currentCol)
                    || isRail(currentTile)
                    || isRail(nextTile)
                    || isStair(currentTile)
                    || isStair(nextTile);
        }

        return false;
    }

    private boolean hasGroundBelow(
            int[][] pathMap,
            TileType[][] tileMap,
            int row,
            int col
    ) {
        int rowBelow = row + 1;

        if (!isInsideBounds(pathMap, rowBelow, col)) {
            return true;
        }

        /*
     * A blocked tile below counts as ground.
     * This includes walls, stones, ice blocks, etc.
         */
        if (isBlocked(pathMap[rowBelow][col])) {
            return true;
        }

        /*
     * MOLTEN also counts as ground below.
     * It is walkable, but it can also support movement above it.
         */
        if (tileMap[rowBelow][col] == TileType.MOLTEN) {
            return true;
        }

        TileType currentTile = tileMap[row][col];

        /*
     * If the enemy is on a stair or rail,
     * it does not need ground below.
         */
        return isStair(currentTile) || isRail(currentTile);
    }

    private Direction rebuildFirstDirection(
            int startRow,
            int startCol,
            int targetRow,
            int targetCol,
            int[][] previousRow,
            int[][] previousCol
    ) {
        int currentRow = targetRow;
        int currentCol = targetCol;

        while (true) {
            int prevRow = previousRow[currentRow][currentCol];
            int prevCol = previousCol[currentRow][currentCol];

            if (prevRow == -1 || prevCol == -1) {
                return null;
            }

            if (prevRow == startRow && prevCol == startCol) {
                int dr = currentRow - startRow;
                int dc = currentCol - startCol;

                for (Direction direction : Direction.values()) {
                    if (direction.getDr() == dr && direction.getDc() == dc) {
                        return direction;
                    }
                }

                return null;
            }

            currentRow = prevRow;
            currentCol = prevCol;
        }
    }

    private int distanceToTarget(int row, int col, int targetRow, int targetCol) {
        return Math.abs(row - targetRow) + Math.abs(col - targetCol);
    }

    private boolean isInsideBounds(int[][] pathMap, int row, int col) {
        return row >= 0
                && row < pathMap.length
                && col >= 0
                && col < pathMap[0].length;
    }

    private boolean isBlocked(int value) {
        /*
         * Compatible with:
         * 1  = blocked tile in a simple grid
         * -2 = BLOCKED value from GameState
         */
        return value == 1 || value == BLOCKED;
    }

    private boolean isStair(TileType tileType) {
        return tileType == TileType.STAIR;
    }

    private boolean isRail(TileType tileType) {
        return tileType == TileType.RAIL;
    }
}