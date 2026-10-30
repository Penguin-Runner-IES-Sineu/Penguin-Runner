namespace iessineu.penguinrunner.Movement;

/*
 * Direccions possibles del moviment.
 *
 * Cada direcció té:
 * dr = canvi de fila
 * dc = canvi de columna
 *
 * Exemple:
 * UP(-1, 0) vol dir:
 * fila - 1, mateixa columna.
 */
public class Direction : IEquatable<Direction>
{
    public static readonly Direction UP = new Direction(-1, 0, "UP");
    public static readonly Direction DOWN = new Direction(1, 0, "DOWN");
    public static readonly Direction LEFT = new Direction(0, -1, "LEFT");
    public static readonly Direction RIGHT = new Direction(0, 1, "RIGHT");

    private readonly int dr;
    private readonly int dc;
    private readonly string name;

    private Direction(int dr, int dc, string name)
    {
        this.dr = dr;
        this.dc = dc;
        this.name = name;
    }

    public int getDr() => dr;
    public int getDc() => dc;

    public int GetDr() => dr;
    public int GetDc() => dc;

    public string getName() => name;

    public static Direction[] values() => new[] { UP, DOWN, LEFT, RIGHT };
    public static Direction[] Values => values();

    public bool Equals(Direction? other)
    {
        if (other is null) return false;
        if (ReferenceEquals(this, other)) return true;
        return dr == other.dr && dc == other.dc;
    }

    public override bool Equals(object? obj) => Equals(obj as Direction);

    public override int GetHashCode() => HashCode.Combine(dr, dc);

    public static bool operator ==(Direction? left, Direction? right)
    {
        if (left is null) return right is null;
        return left.Equals(right);
    }

    public static bool operator !=(Direction? left, Direction? right) => !(left == right);

    public override string ToString() => name;
}

