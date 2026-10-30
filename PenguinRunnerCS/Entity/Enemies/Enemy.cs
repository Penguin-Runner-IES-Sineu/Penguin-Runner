namespace iessineu.penguinrunner.Entity.Enemies;

public class Enemy : Printable
{
    private int row;
    private int col;
    private readonly int respawnRow;
    private readonly int respawnCol;
    private readonly int originalRow;
    private readonly int originalCol;

    private readonly int homeRow;
    private readonly int homeCol;

    private bool _isDead = false;
    private int timeToRevive = 0;

    public Enemy(int row, int col, int respawnRow, int respawnCol)
    {
        originalRow = this.row = row;
        originalCol = this.col = col;
        this.respawnRow = respawnRow;
        this.respawnCol = respawnCol;
        this.homeRow = row;
        this.homeCol = col;

        setPrintables();
    }

    public int getRow() => row;
    public int GetRow() => row;

    public int respawnRow_() => respawnRow;

    public void moveToOriginalRow()
    {
        row = originalRow;
        col = originalCol;
    }

    public void MoveToOriginalRow() => moveToOriginalRow();

    public int getCol() => col;
    public int GetCol() => col;

    public int getRespawnCol() => respawnCol;
    public int GetRespawnCol() => respawnCol;

    public int getRespawnRow() => respawnRow;
    public int GetRespawnRow() => respawnRow;

    public bool isDead() => _isDead;
    public bool IsDead() => _isDead;

    public void revive()
    {
        _isDead = false;
        setPosition(respawnRow, respawnCol);
    }

    public void Revive() => revive();

    public void die()
    {
        _isDead = true;
    }

    public void Die() => die();

    public void setTimeToRevive(int timeToRevive)
    {
        this.timeToRevive = timeToRevive;
    }

    public void SetTimeToRevive(int timeToRevive) => setTimeToRevive(timeToRevive);

    public int getTimeToRevive() => timeToRevive;
    public int GetTimeToRevive() => timeToRevive;

    public void subtractTimeToRevive(int timeLess)
    {
        this.timeToRevive -= timeLess;
    }

    public void SubtractTimeToRevive(int timeLess) => subtractTimeToRevive(timeLess);

    public void setPosition(int row, int col)
    {
        this.row = row;
        this.col = col;
    }

    public void SetPosition(int row, int col) => setPosition(row, col);

    public int getHomeRow() => homeRow;
    public int getHomeCol() => homeCol;

    public int GetHomeRow() => homeRow;
    public int GetHomeCol() => homeCol;

    public virtual void setPrintables()
    {
        base.setPrintables("enemy");
    }

    public void SetPrintables() => setPrintables();
}

