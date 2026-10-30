namespace iessineu.penguinrunner.Entity.Items;

public class Teleport : Item
{
    private bool _pointSet;
    private int row;
    private int col;

    public Teleport() : base("teleport")
    {
    }

    private void setPoint(int row, int col)
    {
        this.row = row;
        this.col = col;
        _pointSet = true;
    }

    public bool use(Player p)
    {
        if (_pointSet)
        {
            p.setPosition(row, col);
            return true;
        }
        setPoint(p.getRow(), p.getCol());
        return false;
    }

    public bool Use(Player p) => use(p);

    public int getRow() => row;
    public int getCol() => col;
    public bool pointSet() => _pointSet;

    public int GetRow() => row;
    public int GetCol() => col;
    public bool PointSet() => _pointSet;
}

