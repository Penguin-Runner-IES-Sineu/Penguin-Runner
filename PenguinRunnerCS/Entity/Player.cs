using iessineu.penguinrunner.Entity.Items;
using iessineu.penguinrunner.Movement;
using iessineu.penguinrunner.States;

namespace iessineu.penguinrunner.Entity;

public class Player : Printable
{
    private int row;
    private int col;

    private readonly int originalRow;
    private readonly int originalCol;

    private int iceCream;
    private int lives = 3;
    private const int MAX_LIVES = 3;
    private PlayerState state;
    private List<Item> items = new();
    private int selectedItem = 0;
    private Direction lastDirection = Direction.RIGHT;

    public Player(int row, int col)
    {
        this.row = row;
        this.col = col;

        this.originalRow = row;
        this.originalCol = col;

        this.lives = MAX_LIVES;

        this.state = new WalkingState();
        this.setPrintables();
    }

    public int getLives() => lives;
    public void setLives(int lives) => this.lives = lives;

    public int GetLives() => lives;
    public void SetLives(int lives) => this.lives = lives;

    public bool isAlive() => lives > 0;
    public bool IsAlive() => lives > 0;

    public void loseLife()
    {
        if (lives > 0)
        {
            lives--;
        }
    }

    public void LoseLife() => loseLife();

    public void setItems(List<Item> itemList)
    {
        this.items = itemList ?? new List<Item>();
    }

    public void SetItems(List<Item> itemList) => setItems(itemList);

    public int getRow() => row;
    public int getCol() => col;

    public int GetRow() => row;
    public int GetCol() => col;

    public void setPosition(int row, int col)
    {
        this.row = row;
        this.col = col;
    }

    public void SetPosition(int row, int col) => setPosition(row, col);

    public void moveToOriginalPosition()
    {
        this.row = originalRow;
        this.col = originalCol;
    }

    public void MoveToOriginalPosition() => moveToOriginalPosition();

    public int getOriginalRow() => originalRow;
    public int getOriginalCol() => originalCol;

    public int GetOriginalRow() => originalRow;
    public int GetOriginalCol() => originalCol;

    public void addIceCream()
    {
        iceCream++;
    }

    public void AddIceCream() => addIceCream();

    public int geticeCream() => iceCream;
    public int GetIceCream() => iceCream;

    public void setIceCream(int icecream)
    {
        this.iceCream = icecream;
    }

    public void SetIceCream(int icecream) => setIceCream(icecream);

    public void addItem(string type)
    {
        switch (type.ToLowerInvariant())
        {
            case "flamethrower":
                Flamethrower f = new Flamethrower();
                items.Add(f);
                Console.WriteLine("Has agafat un lanzallamas!");
                break;
            case "teleport":
                Teleport t = new Teleport();
                items.Add(t);
                Console.WriteLine("Has agafat un teleport!");
                break;
        }
    }

    public void AddItem(string type) => addItem(type);

    public void useItem(GameState gs)
    {
        if (items.Count == 0) return;
        Item item = items[selectedItem];
        switch (item.getName().ToLowerInvariant())
        {
            case "flamethrower":
                Flamethrower f = (Flamethrower)item;
                f.use(this, gs);
                if (f.expired())
                {
                    items.RemoveAt(selectedItem);
                    selectedItem = 0;
                    Console.WriteLine("L'objecte ha acabat els usos");
                }
                break;
        }
    }

    public void UseItem(GameState gs) => useItem(gs);

    public bool hasItem(string type)
    {
        foreach (var item in items)
        {
            if (string.Equals(item.getName(), type, StringComparison.OrdinalIgnoreCase))
            {
                return true;
            }
        }
        return false;
    }

    public bool HasItem(string type) => hasItem(type);

    public void removeItem()
    {
        if (selectedItem >= 0 && selectedItem < items.Count)
        {
            items.RemoveAt(selectedItem);
        }
    }

    public void RemoveItem() => removeItem();

    public PlayerState getState() => state;
    public void setState(PlayerState state) => this.state = state;

    public PlayerState GetState() => state;
    public void SetState(PlayerState state) => this.state = state;

    public void setPrintables()
    {
        base.setPrintables("player");
    }

    public void SetPrintables() => setPrintables();

    public Direction getLastDirection() => lastDirection;
    public void setLastDirection(Direction lastDirection) => this.lastDirection = lastDirection;

    public Direction GetLastDirection() => lastDirection;
    public void SetLastDirection(Direction lastDirection) => this.lastDirection = lastDirection;

    public Item getSelectedItem()
    {
        if (hasItems())
        {
            return items[selectedItem];
        }
        else
        {
            return new Item("");
        }
    }

    public Item GetSelectedItem() => getSelectedItem();

    public int getSelectedItemIndex() => selectedItem;
    public void setSelectedItemIndex(int index) => this.selectedItem = index;

    public int GetSelectedItemIndex() => selectedItem;
    public void SetSelectedItemIndex(int index) => this.selectedItem = index;

    public void nextItem()
    {
        if (items.Count == 0) return;
        this.selectedItem++;
        if (this.selectedItem >= items.Count)
        {
            this.selectedItem = 0;
        }
    }

    public void NextItem() => nextItem();

    public void previousItem()
    {
        if (items.Count == 0) return;
        this.selectedItem--;
        if (this.selectedItem < 0)
        {
            this.selectedItem = items.Count - 1;
        }
    }

    public void PreviousItem() => previousItem();

    public bool hasItems() => items.Count > 0;
    public bool HasItems() => items.Count > 0;

    public List<Item> getItems() => items;
    public List<Item> GetItems() => items;
}

