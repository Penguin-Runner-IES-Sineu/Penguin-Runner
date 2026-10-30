namespace iessineu.penguinrunner.Entity.Enemies;

public class SeekerEnemy : Enemy
{
    private int[][]? pathMap;

    public SeekerEnemy(int row, int col, int respawnRow, int respawnCol)
        : base(row, col, respawnRow, respawnCol)
    {
        setPrintables();
    }

    public int[][]? getPathMap() => pathMap;
    public void setPathMap(int[][]? pathMap) => this.pathMap = pathMap;

    public int[][]? GetPathMap() => pathMap;
    public void SetPathMap(int[][]? pathMap) => this.pathMap = pathMap;

    public override void setPrintables()
    {
        base.setPrintables("seekerenemy");
    }
}

