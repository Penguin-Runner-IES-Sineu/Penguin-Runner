namespace iessineu.penguinrunner.Entity.Items;

public class Item : Printable
{
    protected string name;

    public Item(string name)
    {
        this.name = name;
    }

    public string getName() => name;
    public string GetName() => name;
}

