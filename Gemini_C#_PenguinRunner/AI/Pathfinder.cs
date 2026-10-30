namespace Gemini.PenguinRunner.AI;

using Gemini.PenguinRunner.Core;

public class Pathfinder
{
    private const int MaxCells = 1024;
    private readonly int[] _queue = new int[MaxCells];
    private readonly int[] _visitedToken = new int[MaxCells];
    private readonly int[] _parent = new int[MaxCells];
    private int _currentToken = 1;

    public Direction? FindShortestDirection(Block?[,] blocks, int startRow, int startCol, int targetRow, int targetCol)
    {
        int rows = blocks.GetLength(0);
        int cols = blocks.GetLength(1);

        if (startRow < 0 || startRow >= rows || startCol < 0 || startCol >= cols) return null;
        if (targetRow < 0 || targetRow >= rows || targetCol < 0 || targetCol >= cols) return null;
        if (startRow == targetRow && startCol == targetCol) return null;

        _currentToken++;
        if (_currentToken == int.MaxValue)
        {
            Array.Clear(_visitedToken, 0, _visitedToken.Length);
            _currentToken = 1;
        }

        int head = 0;
        int tail = 0;

        int startIdx = startRow * cols + startCol;
        int targetIdx = targetRow * cols + targetCol;

        _queue[tail++] = startIdx;
        _visitedToken[startIdx] = _currentToken;
        _parent[startIdx] = -1;

        bool found = false;
        int bestIdx = startIdx;
        int minDistance = Math.Abs(startRow - targetRow) + Math.Abs(startCol - targetCol);

        ReadOnlySpan<Direction> directions = [Direction.Up, Direction.Down, Direction.Left, Direction.Right];

        while (head < tail)
        {
            int curr = _queue[head++];
            int r = curr / cols;
            int c = curr % cols;

            if (curr == targetIdx)
            {
                found = true;
                bestIdx = curr;
                break;
            }

            int dist = Math.Abs(r - targetRow) + Math.Abs(c - targetCol);
            if (dist < minDistance)
            {
                minDistance = dist;
                bestIdx = curr;
            }

            foreach (var dir in directions)
            {
                int nr = r + dir.DRow();
                int nc = c + dir.DCol();
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;

                int nIdx = nr * cols + nc;
                if (_visitedToken[nIdx] == _currentToken) continue;

                if (!CanTraverse(blocks, r, c, nr, nc, dir)) continue;

                _visitedToken[nIdx] = _currentToken;
                _parent[nIdx] = curr;
                _queue[tail++] = nIdx;
            }
        }

        int destination = found ? targetIdx : bestIdx;
        if (destination == startIdx) return null;

        int step = destination;
        while (_parent[step] != -1 && _parent[step] != startIdx)
        {
            step = _parent[step];
        }

        if (_parent[step] != startIdx) return null;

        int stepRow = step / cols;
        int stepCol = step % cols;

        foreach (var dir in directions)
        {
            if (startRow + dir.DRow() == stepRow && startCol + dir.DCol() == stepCol)
            {
                return dir;
            }
        }

        return null;
    }

    private static bool CanTraverse(Block?[,] blocks, int r, int c, int nr, int nc, Direction dir)
    {
        var target = blocks[nr, nc];
        if (target == null || target.IsSolid) return false;

        var current = blocks[r, c];
        bool currentIsStair = (current != null && current.Type == TileType.Stair);
        bool targetIsStair = (target.Type == TileType.Stair);
        bool currentIsRail = (current != null && current.Type == TileType.Rail);

        int rows = blocks.GetLength(0);
        var below = (r + 1 < rows) ? blocks[r + 1, c] : null;
        bool onGround = (below != null && (below.IsSolid || below.Type == TileType.Stair));

        return dir switch
        {
            Direction.Up => currentIsStair,
            Direction.Down => targetIsStair || !onGround || currentIsStair,
            _ => onGround || currentIsStair || currentIsRail
        };
    }
}
