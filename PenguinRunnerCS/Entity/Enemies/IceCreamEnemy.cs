namespace iessineu.penguinrunner.Entity.Enemies;

public class IceCreamEnemy : Enemy
{
    private bool _hasIceCream = false;

    public IceCreamEnemy(int row, int col, int respawnRow, int respawnCol)
        : base(row, col, respawnRow, respawnCol)
    {
        setPrintables();
    }

    public bool hasIceCream() => _hasIceCream;
    public bool HasIceCream() => _hasIceCream;

    public void collectIceCream()
    {
        _hasIceCream = true;
    }

    public void CollectIceCream() => collectIceCream();

    public void dropIceCream()
    {
        _hasIceCream = false;
    }

    public void DropIceCream() => dropIceCream();

    public override void setPrintables()
    {
        base.setPrintables("icecreamenemy");
    }
}

