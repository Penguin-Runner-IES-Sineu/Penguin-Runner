namespace Gemini.PenguinRunner.Entities;

using Gemini.PenguinRunner.Core;

public abstract class Entity
{
    public int Row { get; protected set; }
    public int Col { get; protected set; }
    public int StartRow { get; }
    public int StartCol { get; }
    public Direction LastDirection { get; set; } = Direction.Right;
    public bool IsDead { get; set; }

    protected Entity(int row, int col)
    {
        Row = row;
        Col = col;
        StartRow = row;
        StartCol = col;
    }

    public void SetPosition(int row, int col)
    {
        Row = row;
        Col = col;
    }

    public virtual void ResetToStart()
    {
        Row = StartRow;
        Col = StartCol;
        IsDead = false;
    }
}
