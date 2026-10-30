using System.Text.Json;
using Gemini.PenguinRunner.AI;
using Gemini.PenguinRunner.Assets;
using Gemini.PenguinRunner.Entities;
using Gemini.PenguinRunner.Items;
using Gemini.PenguinRunner.States;

namespace Gemini.PenguinRunner.Core;

public class World
{
    private readonly string _resourceDir;
    private readonly AssetManager _assets;
    private readonly Pathfinder _pathfinder = new();
    private readonly List<string[]> _mapLevels = new();

    public AssetManager Audio => _assets;
    public Block?[,] Blocks { get; private set; } = new Block?[0, 0];
    public int Rows => Blocks.GetLength(0);
    public int Cols => Blocks.GetLength(1);

    public Player? Player { get; private set; }
    public List<Enemy> Enemies { get; } = new();

    public int CurrentLevelIndex { get; private set; }
    public int CurrentLevel => CurrentLevelIndex + 1;
    public int TotalIceCream { get; private set; }
    public bool ButtonPressed { get; private set; }
    public bool LevelCleared { get; private set; }
    public bool GameOver { get; set; }

    private readonly WalkingState _walkingState = new();
    private readonly ClimbingState _climbingState = new();
    private readonly RailState _railState = new();
    private readonly FallingState _fallingState = new();

    public World(string resourceDir, AssetManager assets)
    {
        _resourceDir = resourceDir;
        _assets = assets;
        LoadMapFile();
    }

    private void LoadMapFile()
    {
        string mapPath = Path.Combine(_resourceDir, "maps.json");
        if (!File.Exists(mapPath)) return;

        try
        {
            string json = File.ReadAllText(mapPath);
            using var doc = JsonDocument.Parse(json);
            foreach (var elem in doc.RootElement.EnumerateArray())
            {
                JsonElement viewElem;
                if (!elem.TryGetProperty("view", out viewElem) && !elem.TryGetProperty("map", out viewElem))
                {
                    continue;
                }

                int count = viewElem.GetArrayLength();
                string[] lines = new string[count];
                for (int i = 0; i < count; i++)
                {
                    lines[i] = viewElem[i].GetString() ?? "";
                }
                _mapLevels.Add(lines);
            }
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine($"Error reading maps.json: {ex.Message}");
        }
    }

    public bool InitLevel(int levelIdx)
    {
        if (_mapLevels.Count == 0) return false;
        CurrentLevelIndex = Math.Clamp(levelIdx, 0, _mapLevels.Count - 1);
        string[] levelLines = _mapLevels[CurrentLevelIndex];

        int rows = levelLines.Length;
        int cols = levelLines.Max(l => l.Length);

        Blocks = new Block?[rows, cols];
        Enemies.Clear();
        TotalIceCream = 0;
        ButtonPressed = false;
        LevelCleared = false;
        GameOver = false;

        var preservedItems = Player != null ? new List<IItem>(Player.Items) : new List<IItem>();
        int preservedLives = Player?.Lives ?? Player.MaxLives;

        for (int r = 0; r < rows; r++)
        {
            string line = levelLines[r];
            for (int c = 0; c < cols; c++)
            {
                char ch = c < line.Length ? line.charAt(c) : ' ';
                Block block;

                switch (ch)
                {
                    case '#': block = new Block(TileType.Ice, r, c); break;
                    case '=': block = new Block(TileType.Wall, r, c); break;
                    case 'H': block = new Block(TileType.Stair, r, c); break;
                    case '-': block = new Block(TileType.Rail, r, c); break;
                    case 'S': block = new Block(TileType.Stone, r, c); break;
                    case 'W': block = new Block(TileType.Wood, r, c); break;
                    case 'D': block = new Block(TileType.Door, r, c); break;
                    case 'T' or 'b': block = new Block(TileType.Trapdoor, r, c); break;
                    case 'B': block = new Block(TileType.Button, r, c); break;
                    case 'G':
                        block = new Block(TileType.IceCream, r, c);
                        TotalIceCream++;
                        break;
                    case 'F': block = new Block(TileType.Flamethrower, r, c); break;
                    case 'P':
                        Player = new Player(r, c) { Lives = preservedLives };
                        foreach (var item in preservedItems) Player.AddItem(item);
                        block = new Block(TileType.Blank, r, c);
                        break;
                    case 'E':
                        Enemies.Add(new SeekerEnemy(r, c, 1, 1));
                        block = new Block(TileType.Blank, r, c);
                        break;
                    case 'A':
                        Enemies.Add(new AmbushingEnemy(r, c, 1, 1));
                        block = new Block(TileType.Blank, r, c);
                        break;
                    case 'C':
                        Enemies.Add(new IceCreamEnemy(r, c, 1, 1));
                        block = new Block(TileType.Blank, r, c);
                        break;
                    default: block = new Block(TileType.Blank, r, c); break;
                }
                Blocks[r, c] = block;
            }
        }

        Player ??= new Player(1, 1) { Lives = preservedLives };

        IceAutoTiler.UpdateAll(Blocks);
        UpdatePlayerState();
        return true;
    }

    public void NextLevel()
    {
        if (CurrentLevelIndex + 1 < _mapLevels.Count)
        {
            InitLevel(CurrentLevelIndex + 1);
        }
        else
        {
            InitLevel(0);
        }
    }

    public void RestartLevel() => InitLevel(CurrentLevelIndex);

    public bool IsInBounds(int r, int c) => r >= 0 && r < Rows && c >= 0 && c < Cols;

    public Block? GetBlock(int r, int c) => IsInBounds(r, c) ? Blocks[r, c] : null;

    public void HandlePlayerInput(Direction dir)
    {
        if (GameOver || LevelCleared || Player == null || !Player.IsAlive) return;

        Player.LastDirection = dir;
        Player.State.HandleInput(this, dir);
        StepTurn();
    }

    public void PassTurn()
    {
        if (GameOver || LevelCleared || Player == null || !Player.IsAlive) return;
        StepTurn();
    }

    public void MovePlayer(Direction dir, bool canPush)
    {
        if (Player == null) return;
        int nr = Player.Row + dir.DRow();
        int nc = Player.Col + dir.DCol();
        if (!IsInBounds(nr, nc)) return;

        var target = Blocks[nr, nc];
        if (target != null && target.IsPushable && canPush)
        {
            if (!TryPushBlock(nr, nc, dir)) return;
        }

        if (CanPass(nr, nc))
        {
            Player.SetPosition(nr, nc);
            InteractWithTile(nr, nc);
        }
    }

    public void MovePlayerDownOne()
    {
        if (Player == null) return;
        int nr = Player.Row + 1;
        int nc = Player.Col;
        if (IsInBounds(nr, nc) && CanPass(nr, nc))
        {
            Player.SetPosition(nr, nc);
            InteractWithTile(nr, nc);
        }
    }

    public bool CanPass(int r, int c)
    {
        if (!IsInBounds(r, c)) return false;
        var b = Blocks[r, c];
        return b == null || !b.IsSolid;
    }

    private bool TryPushBlock(int r, int c, Direction dir)
    {
        int dc = dir.DCol();
        if (dc == 0) return false;

        int checkCol = c;
        while (IsInBounds(r, checkCol) && Blocks[r, checkCol]!.IsPushable)
        {
            checkCol += dc;
        }

        if (!IsInBounds(r, checkCol) || Blocks[r, checkCol]!.IsSolid)
        {
            return false;
        }

        int fromCol = checkCol - dc;
        while (fromCol != c - dc)
        {
            Blocks[r, fromCol + dc] = Blocks[r, fromCol];
            fromCol -= dc;
        }

        Blocks[r, c] = new Block(TileType.Blank, r, c);
        IceAutoTiler.UpdateDirtyNeighbors(Blocks, r, c);
        IceAutoTiler.UpdateDirtyNeighbors(Blocks, r, checkCol);
        return true;
    }

    private void InteractWithTile(int r, int c)
    {
        var b = Blocks[r, c];
        if (b == null) return;

        switch (b.Type)
        {
            case TileType.IceCream:
                if (Player != null) Player.CollectedIceCream++;
                b.Type = TileType.Blank;
                _assets.PlaySound("collect");
                CheckObjective();
                break;
            case TileType.Flamethrower:
                Player?.AddItem(new Flamethrower());
                b.Type = TileType.Blank;
                _assets.PlaySound("pickup");
                break;
            case TileType.Teleport:
                Player?.AddItem(new Teleport());
                b.Type = TileType.Blank;
                _assets.PlaySound("pickup");
                break;
            case TileType.Button:
                ButtonPressed = !ButtonPressed;
                for (int row = 0; row < Rows; row++)
                {
                    for (int col = 0; col < Cols; col++)
                    {
                        var blk = Blocks[row, col];
                        if (blk != null)
                        {
                            blk.ButtonPressed = ButtonPressed;
                            if (blk.IsTrapdoor)
                            {
                                blk.Type = ButtonPressed ? TileType.Trapdoor : TileType.Blank;
                            }
                        }
                    }
                }
                _assets.PlaySound("button");
                break;
            case TileType.Door:
                if (b.DoorOpen)
                {
                    LevelCleared = true;
                    _assets.PlaySound("door");
                    NextLevel();
                }
                break;
        }
    }

    public void CheckObjective()
    {
        if (Player != null && Player.CollectedIceCream >= TotalIceCream)
        {
            for (int r = 0; r < Rows; r++)
            {
                for (int c = 0; c < Cols; c++)
                {
                    var b = Blocks[r, c];
                    if (b != null && b.IsDoor)
                    {
                        b.Type = TileType.Door;
                        b.DoorOpen = true;
                    }
                }
            }
        }
    }

    public void BreakBlock(int dCol)
    {
        if (Player == null) return;
        int br = Player.Row + 1;
        int bc = Player.Col + dCol;

        if (IsInBounds(br, bc))
        {
            var b = Blocks[br, bc];
            if (b != null && b.Type == TileType.Ice && b.IsBreakable)
            {
                b.Type = TileType.Molten;
                b.RegenTurns = 6;
                IceAutoTiler.UpdateDirtyNeighbors(Blocks, br, bc);
                _assets.PlaySound("popice");
                StepTurn();
            }
        }
    }

    public void UseItem()
    {
        if (Player == null || !Player.HasItems) return;
        var item = Player.GetSelectedItem();
        if (item != null && item.Use(Player, this))
        {
            Player.CleanExpiredItems();
            StepTurn();
        }
    }

    public void StepTurn()
    {
        UpdateBrokenBlocks();
        MoveEnemies();
        CheckCollisions();
        UpdatePlayerState();
    }

    private void UpdateBrokenBlocks()
    {
        for (int r = 0; r < Rows; r++)
        {
            for (int c = 0; c < Cols; c++)
            {
                var b = Blocks[r, c];
                if (b != null && b.Type == TileType.Molten)
                {
                    b.DecrementRegen();
                    if (b.Type == TileType.Ice)
                    {
                        IceAutoTiler.UpdateDirtyNeighbors(Blocks, r, c);
                    }
                }
            }
        }
    }

    private void MoveEnemies()
    {
        foreach (var enemy in Enemies)
        {
            if (enemy.IsDead)
            {
                enemy.TickRevive();
                continue;
            }

            var currBlock = GetBlock(enemy.Row, enemy.Col);
            if (currBlock != null && currBlock.Type == TileType.Molten)
            {
                enemy.Kill(10);
                continue;
            }

            var dir = enemy.DecideMove(this, _pathfinder);
            if (dir.HasValue)
            {
                int nr = enemy.Row + dir.Value.DRow();
                int nc = enemy.Col + dir.Value.DCol();
                if (IsInBounds(nr, nc) && CanPass(nr, nc))
                {
                    enemy.SetPosition(nr, nc);
                    enemy.LastDirection = dir.Value;
                }
            }
        }
    }

    public void CheckCollisions()
    {
        if (Player == null || !Player.IsAlive) return;

        foreach (var enemy in Enemies)
        {
            if (!enemy.IsDead && enemy.Row == Player.Row && enemy.Col == Player.Col)
            {
                PlayerDied();
                break;
            }
        }
    }

    public void PlayerDied()
    {
        if (Player == null) return;
        Player.LoseLife();
        _assets.PlaySound("death");

        if (Player.Lives <= 0)
        {
            GameOver = true;
            _assets.PlaySound("gameover");
        }
        else
        {
            ResetPositionsAfterDeath();
        }
    }

    private void ResetPositionsAfterDeath()
    {
        Player?.ResetToStart();
        foreach (var enemy in Enemies)
        {
            enemy.ResetToStart();
        }
        UpdatePlayerState();
    }

    public void UpdatePlayerState()
    {
        if (Player == null) return;
        int r = Player.Row;
        int c = Player.Col;

        var curr = GetBlock(r, c);
        var below = GetBlock(r + 1, c);

        if (curr != null && curr.Type == TileType.Stair)
        {
            Player.State = _climbingState;
        }
        else if (curr != null && curr.Type == TileType.Rail)
        {
            Player.State = _railState;
        }
        else if (below == null || (!below.IsSolid && below.Type != TileType.Stair))
        {
            Player.State = _fallingState;
        }
        else
        {
            Player.State = _walkingState;
        }
    }
}

file static class StringExtensions
{
    public static char charAt(this string s, int index) => s[index];
}
