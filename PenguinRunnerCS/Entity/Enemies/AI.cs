using iessineu.penguinrunner.Blocks;
using iessineu.penguinrunner.Movement;

namespace iessineu.penguinrunner.Entity.Enemies;

public class AI
{
    private const int BLOCKED = -2;

    private class Node
    {
        public int row;
        public int col;

        public Node(int row, int col)
        {
            this.row = row;
            this.col = col;
        }
    }

    public Direction? getShortestDirection(
        int[][] pathMap,
        TileType[][] tileMap,
        int startRow,
        int startCol,
        int targetRow,
        int targetCol
    )
    {
        if (pathMap == null || pathMap.Length == 0 || pathMap[0].Length == 0)
        {
            return null;
        }

        if (tileMap == null || tileMap.Length == 0 || tileMap[0].Length == 0)
        {
            return null;
        }

        if (!isInsideBounds(pathMap, startRow, startCol))
        {
            return null;
        }

        if (!isInsideBounds(pathMap, targetRow, targetCol))
        {
            return null;
        }

        if (isBlocked(pathMap[startRow][startCol]))
        {
            return null;
        }

        int rows = pathMap.Length;
        int cols = pathMap[0].Length;

        bool[,] visited = new bool[rows, cols];

        int[,] previousRow = new int[rows, cols];
        int[,] previousCol = new int[rows, cols];

        for (int row = 0; row < rows; row++)
        {
            for (int col = 0; col < cols; col++)
            {
                previousRow[row, col] = -1;
                previousCol[row, col] = -1;
            }
        }

        Queue<Node> queue = new();

        visited[startRow, startCol] = true;
        queue.Enqueue(new Node(startRow, startCol));

        bool foundTarget = false;

        int bestRow = startRow;
        int bestCol = startCol;
        int bestDistanceToTarget = distanceToTarget(startRow, startCol, targetRow, targetCol);

        while (queue.Count > 0)
        {
            Node current = queue.Dequeue();

            int dist = distanceToTarget(
                current.row,
                current.col,
                targetRow,
                targetCol
            );

            if (dist < bestDistanceToTarget)
            {
                bestDistanceToTarget = dist;
                bestRow = current.row;
                bestCol = current.col;
            }

            if (current.row == targetRow && current.col == targetCol)
            {
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

        if (!foundTarget)
        {
            if (bestRow == startRow && bestCol == startCol)
            {
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

    public Direction? GetShortestDirection(
        int[][] pathMap,
        TileType[][] tileMap,
        int startRow,
        int startCol,
        int targetRow,
        int targetCol
    ) => getShortestDirection(pathMap, tileMap, startRow, startCol, targetRow, targetCol);

    private void tryMove(
        int[][] pathMap,
        TileType[][] tileMap,
        Queue<Node> queue,
        bool[,] visited,
        int[,] previousRow,
        int[,] previousCol,
        int currentRow,
        int currentCol,
        Direction direction
    )
    {
        int nextRow = currentRow + direction.getDr();
        int nextCol = currentCol + direction.getDc();

        if (!isInsideBounds(pathMap, nextRow, nextCol))
        {
            return;
        }

        if (visited[nextRow, nextCol])
        {
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
        ))
        {
            return;
        }

        visited[nextRow, nextCol] = true;

        previousRow[nextRow, nextCol] = currentRow;
        previousCol[nextRow, nextCol] = currentCol;

        queue.Enqueue(new Node(nextRow, nextCol));
    }

    private bool canMove(
        int[][] pathMap,
        TileType[][] tileMap,
        int currentRow,
        int currentCol,
        int nextRow,
        int nextCol,
        Direction direction
    )
    {
        if (!isInsideBounds(pathMap, nextRow, nextCol))
        {
            return false;
        }

        if (isBlocked(pathMap[nextRow][nextCol]))
        {
            return false;
        }

        TileType currentTile = tileMap[currentRow][currentCol];
        TileType nextTile = tileMap[nextRow][nextCol];

        if (direction == Direction.UP)
        {
            return isStair(currentTile) || isStair(nextTile);
        }

        if (direction == Direction.DOWN)
        {
            return isStair(currentTile)
                || isStair(nextTile)
                || !hasGroundBelow(pathMap, tileMap, currentRow, currentCol);
        }

        if (direction == Direction.LEFT || direction == Direction.RIGHT)
        {
            return hasGroundBelow(pathMap, tileMap, nextRow, nextCol)
                || hasGroundBelow(pathMap, tileMap, currentRow, currentCol)
                || isRail(currentTile)
                || isRail(nextTile)
                || isStair(currentTile)
                || isStair(nextTile);
        }

        return false;
    }

    private bool hasGroundBelow(
        int[][] pathMap,
        TileType[][] tileMap,
        int row,
        int col
    )
    {
        int rowBelow = row + 1;

        if (!isInsideBounds(pathMap, rowBelow, col))
        {
            return true;
        }

        if (isBlocked(pathMap[rowBelow][col]))
        {
            return true;
        }

        if (tileMap[rowBelow][col] == TileType.MOLTEN)
        {
            return true;
        }

        TileType currentTile = tileMap[row][col];

        return isStair(currentTile) || isRail(currentTile);
    }

    private Direction? rebuildFirstDirection(
        int startRow,
        int startCol,
        int targetRow,
        int targetCol,
        int[,] previousRow,
        int[,] previousCol
    )
    {
        int currentRow = targetRow;
        int currentCol = targetCol;

        while (true)
        {
            int prevRow = previousRow[currentRow, currentCol];
            int prevCol = previousCol[currentRow, currentCol];

            if (prevRow == -1 || prevCol == -1)
            {
                return null;
            }

            if (prevRow == startRow && prevCol == startCol)
            {
                int dr = currentRow - startRow;
                int dc = currentCol - startCol;

                foreach (Direction direction in Direction.values())
                {
                    if (direction.getDr() == dr && direction.getDc() == dc)
                    {
                        return direction;
                    }
                }

                return null;
            }

            currentRow = prevRow;
            currentCol = prevCol;
        }
    }

    private int distanceToTarget(int row, int col, int targetRow, int targetCol)
    {
        return Math.Abs(row - targetRow) + Math.Abs(col - targetCol);
    }

    private bool isInsideBounds(int[][] pathMap, int row, int col)
    {
        return row >= 0
            && row < pathMap.Length
            && col >= 0
            && col < pathMap[0].Length;
    }

    private bool isBlocked(int value)
    {
        return value == 1 || value == BLOCKED;
    }

    private bool isStair(TileType tileType) => tileType == TileType.STAIR;
    private bool isRail(TileType tileType) => tileType == TileType.RAIL;
}

