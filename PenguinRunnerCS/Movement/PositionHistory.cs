namespace iessineu.penguinrunner.Movement;

public class PositionHistory
{
    private int x;
    private int y;

    public PositionHistory(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int getX() => x;
    public int getY() => y;
    public void setX(int x) => this.x = x;
    public void setY(int y) => this.y = y;

    public int GetX() => x;
    public int GetY() => y;
    public void SetX(int x) => this.x = x;
    public void SetY(int y) => this.y = y;
}

