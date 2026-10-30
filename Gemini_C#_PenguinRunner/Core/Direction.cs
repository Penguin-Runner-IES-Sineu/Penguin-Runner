namespace Gemini.PenguinRunner.Core;

public enum Direction
{
    Up,
    Down,
    Left,
    Right
}

public static class DirectionExtensions
{
    public static int DRow(this Direction dir) => dir switch
    {
        Direction.Up => -1,
        Direction.Down => 1,
        _ => 0
    };

    public static int DCol(this Direction dir) => dir switch
    {
        Direction.Left => -1,
        Direction.Right => 1,
        _ => 0
    };

    public static Direction Opposite(this Direction dir) => dir switch
    {
        Direction.Up => Direction.Down,
        Direction.Down => Direction.Up,
        Direction.Left => Direction.Right,
        Direction.Right => Direction.Left,
        _ => dir
    };
}
