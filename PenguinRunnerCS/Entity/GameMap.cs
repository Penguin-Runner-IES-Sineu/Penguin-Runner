namespace iessineu.penguinrunner.Entity;

public class GameMap
{
    private readonly string[] map;

    public GameMap(string[] map)
    {
        this.map = map;
    }

    public string[] getMap() => map;
    public string[] GetMap() => map;

    public override string ToString()
    {
        return map.Length > 0 ? map[0] : "";
    }
}

