using iessineu.penguinrunner.Blocks;
using iessineu.penguinrunner.Movement;

namespace iessineu.penguinrunner.Entity.Items;

public class Flamethrower : Item
{
    private int usesLeft;

    public Flamethrower() : base("flamethrower")
    {
        this.usesLeft = 3;
    }

    public void use(Player p, GameState gs)
    {
        usesLeft--;
        int playerRow = p.getRow();
        int playerCol = p.getCol();
        Direction dir = p.getLastDirection();

        if (dir == Direction.UP)
        {
            burnUp(playerRow, playerCol, gs);
        }
        else if (dir == Direction.DOWN)
        {
            burnDown(playerRow, playerCol, gs);
        }
        else if (dir == Direction.LEFT)
        {
            burnLeft(playerRow, playerCol, gs);
        }
        else if (dir == Direction.RIGHT)
        {
            burnRight(playerRow, playerCol, gs);
        }

        Console.WriteLine("Te queden " + usesLeft + " usos");
    }

    public void Use(Player p, GameState gs) => use(p, gs);

    public int getUsesLeft() => usesLeft;
    public int GetUsesLeft() => usesLeft;

    public bool expired() => usesLeft <= 0;
    public bool Expired() => expired();

    public void burnBlock(Block? b)
    {
        if (b != null && b.getType() == TileType.MOLTEN)
        {
            b.setType(TileType.BURNT);
            b.setFire(true);
            b.setPrintables();
            b.setType(TileType.MOLTEN);
        }
    }

    public void BurnBlock(Block? b) => burnBlock(b);

    public void burnUp(int playerRow, int playerCol, GameState gs)
    {
        gs.breakBlock(playerRow - 1, playerCol + 1);
        gs.breakBlock(playerRow - 1, playerCol);
        gs.breakBlock(playerRow - 1, playerCol - 1);
        Block[][] map = gs.getBlocks();
        if (playerRow - 1 >= 0 && playerRow - 1 < map.Length)
        {
            if (playerCol + 1 >= 0 && playerCol + 1 < map[0].Length) burnBlock(map[playerRow - 1][playerCol + 1]);
            if (playerCol >= 0 && playerCol < map[0].Length) burnBlock(map[playerRow - 1][playerCol]);
            if (playerCol - 1 >= 0 && playerCol - 1 < map[0].Length) burnBlock(map[playerRow - 1][playerCol - 1]);
        }
    }

    public void burnDown(int playerRow, int playerCol, GameState gs)
    {
        gs.breakBlock(playerRow + 1, playerCol + 1);
        gs.breakBlock(playerRow + 1, playerCol);
        gs.breakBlock(playerRow + 1, playerCol - 1);
        Block[][] map = gs.getBlocks();
        if (playerRow + 1 >= 0 && playerRow + 1 < map.Length)
        {
            if (playerCol + 1 >= 0 && playerCol + 1 < map[0].Length) burnBlock(map[playerRow + 1][playerCol + 1]);
            if (playerCol >= 0 && playerCol < map[0].Length) burnBlock(map[playerRow + 1][playerCol]);
            if (playerCol - 1 >= 0 && playerCol - 1 < map[0].Length) burnBlock(map[playerRow + 1][playerCol - 1]);
        }
    }

    public void burnRight(int playerRow, int playerCol, GameState gs)
    {
        gs.breakBlock(playerRow + 1, playerCol + 1);
        gs.breakBlock(playerRow + 1, playerCol + 2);
        gs.breakBlock(playerRow + 1, playerCol + 3);
        Block[][] map = gs.getBlocks();
        if (playerRow + 1 >= 0 && playerRow + 1 < map.Length)
        {
            if (playerCol + 1 >= 0 && playerCol + 1 < map[0].Length) burnBlock(map[playerRow + 1][playerCol + 1]);
            if (playerCol + 2 >= 0 && playerCol + 2 < map[0].Length) burnBlock(map[playerRow + 1][playerCol + 2]);
            if (playerCol + 3 >= 0 && playerCol + 3 < map[0].Length) burnBlock(map[playerRow + 1][playerCol + 3]);
        }
    }

    public void burnLeft(int playerRow, int playerCol, GameState gs)
    {
        gs.breakBlock(playerRow + 1, playerCol - 1);
        gs.breakBlock(playerRow + 1, playerCol - 2);
        gs.breakBlock(playerRow + 1, playerCol - 3);
        Block[][] map = gs.getBlocks();
        if (playerRow + 1 >= 0 && playerRow + 1 < map.Length)
        {
            if (playerCol - 1 >= 0 && playerCol - 1 < map[0].Length) burnBlock(map[playerRow + 1][playerCol - 1]);
            if (playerCol - 2 >= 0 && playerCol - 2 < map[0].Length) burnBlock(map[playerRow + 1][playerCol - 2]);
            if (playerCol - 3 >= 0 && playerCol - 3 < map[0].Length) burnBlock(map[playerRow + 1][playerCol - 3]);
        }
    }
}

