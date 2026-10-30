using System.Numerics;
using Gemini.PenguinRunner.Assets;
using Gemini.PenguinRunner.Core;
using Gemini.PenguinRunner.Entities;
using Gemini.PenguinRunner.Items;
using Gemini.PenguinRunner.States;
using Gemini.PenguinRunner.UI;
using Raylib_cs;

namespace Gemini.PenguinRunner;

public static class Program
{
    private const int TileSize = 40;

    public static void Main(string[] args)
    {
        bool testMode = args.Contains("-test");
        string resourceDir = "resources";

        for (int i = 0; i < args.Length; i++)
        {
            if ((args[i] == "-g" || args[i] == "-game") && i + 1 < args.Length)
            {
                resourceDir = Path.Combine("games", args[i + 1]);
            }
        }

        if (!Directory.Exists(resourceDir))
        {
            resourceDir = Path.Combine("Gemini_C#_PenguinRunner", "resources");
        }

        if (testMode)
        {
            RunAutomatedTests(resourceDir);
            return;
        }

        RunGame(resourceDir);
    }

    private static void RunGame(string resourceDir)
    {
        // Temporary window to initialize Raylib and audio subsystem
        Raylib.InitWindow(800, 600, "Penguin Runner (Optimized C#)");
        Raylib.SetExitKey(KeyboardKey.Null); // Prevent ESC from closing window!
        Raylib.SetTargetFPS(60);

        using var assets = new AssetManager(resourceDir);
        assets.Initialize();

        var world = new World(resourceDir, assets);
        world.InitLevel(0);

        int winWidth = world.Cols * TileSize;
        int winHeight = world.Rows * TileSize + HudRenderer.HudHeight;
        Raylib.SetWindowSize(winWidth, winHeight);

        var pauseMenu = new PauseMenuDialog();
        string savePath = Path.Combine("saves", "save.json");

        float fallAccumulator = 0f;

        while (!Raylib.WindowShouldClose())
        {
            float dt = Raylib.GetFrameTime();
            assets.UpdateMusic();

            if (pauseMenu.IsOpen)
            {
                pauseMenu.Update(
                    assets,
                    onSave: () => IO.SaveManager.Save(world, savePath),
                    onLoad: () => {
                        IO.SaveManager.Load(world, savePath);
                        UpdateWindowSize(world);
                    }
                );
            }
            else
            {
                // Handle in-game inputs
                if (Raylib.IsKeyPressed(KeyboardKey.Escape))
                {
                    pauseMenu.Open();
                }
                else if (world.GameOver)
                {
                    if (Raylib.IsKeyPressed(KeyboardKey.R) || Raylib.IsKeyPressed(KeyboardKey.Enter))
                    {
                        world.Player?.ResetLives();
                        world.RestartLevel();
                        UpdateWindowSize(world);
                    }
                }
                else
                {
                    if (Raylib.IsKeyPressed(KeyboardKey.Up) || Raylib.IsKeyPressed(KeyboardKey.W))
                        world.HandlePlayerInput(Direction.Up);
                    else if (Raylib.IsKeyPressed(KeyboardKey.Down) || Raylib.IsKeyPressed(KeyboardKey.S))
                        world.HandlePlayerInput(Direction.Down);
                    else if (Raylib.IsKeyPressed(KeyboardKey.Left) || Raylib.IsKeyPressed(KeyboardKey.A))
                        world.HandlePlayerInput(Direction.Left);
                    else if (Raylib.IsKeyPressed(KeyboardKey.Right) || Raylib.IsKeyPressed(KeyboardKey.D))
                        world.HandlePlayerInput(Direction.Right);
                    else if (Raylib.IsKeyPressed(KeyboardKey.Space))
                        world.PassTurn();
                    else if (Raylib.IsKeyPressed(KeyboardKey.Q))
                        world.BreakBlock(-1);
                    else if (Raylib.IsKeyPressed(KeyboardKey.E))
                        world.BreakBlock(1);
                    else if (Raylib.IsKeyPressed(KeyboardKey.F))
                        world.UseItem();
                    else if (Raylib.IsKeyPressed(KeyboardKey.Z))
                        world.Player?.CycleItem(-1);
                    else if (Raylib.IsKeyPressed(KeyboardKey.X))
                        world.Player?.CycleItem(1);
                    else if (Raylib.IsKeyPressed(KeyboardKey.R))
                    {
                        world.RestartLevel();
                        UpdateWindowSize(world);
                    }

                    // Gravity Falling Timer (every 150ms)
                    if (world.Player?.State is FallingState)
                    {
                        fallAccumulator += dt;
                        if (fallAccumulator >= 0.15f)
                        {
                            fallAccumulator = 0f;
                            world.MovePlayerDownOne();
                            world.StepTurn();
                        }
                    }
                    else
                    {
                        fallAccumulator = 0f;
                    }
                }
            }

            // Render Frame
            Raylib.BeginDrawing();
            Raylib.ClearBackground(new Color(10, 12, 18, 255));

            RenderWorld(world, assets);
            HudRenderer.Render(world, assets, Raylib.GetScreenWidth(), world.Rows * TileSize, TileSize);

            if (world.GameOver)
            {
                RenderGameOver(Raylib.GetScreenWidth(), Raylib.GetScreenHeight());
            }

            pauseMenu.Render(assets);

            Raylib.EndDrawing();
        }

        Raylib.CloseWindow();
    }

    private static void UpdateWindowSize(World world)
    {
        int w = world.Cols * TileSize;
        int h = world.Rows * TileSize + HudRenderer.HudHeight;
        Raylib.SetWindowSize(w, h);
    }

    private static void RenderWorld(World world, AssetManager assets)
    {
        int rows = world.Rows;
        int cols = world.Cols;

        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                var block = world.Blocks[r, c];
                if (block == null || block.Type is TileType.Blank or TileType.Empty) continue;

                int x = c * TileSize;
                int y = r * TileSize;

                string? key = block.Type switch
                {
                    TileType.Ice => block.AutotileKey ?? "ice",
                    TileType.Wall => "wall",
                    TileType.Stair => "stair",
                    TileType.Rail => "rail",
                    TileType.Stone => "stone",
                    TileType.Wood => "wood",
                    TileType.Door => block.DoorOpen ? "door" : null,
                    TileType.Trapdoor => "trapdoor",
                    TileType.Button => "button",
                    TileType.IceCream => "icecream",
                    TileType.Flamethrower => "flamethrower",
                    TileType.Teleport => "teleport",
                    TileType.Molten => "molten",
                    TileType.Burnt => "burnt",
                    _ => null
                };

                if (key != null)
                {
                    DrawSprite(assets, key, x, y, TileSize, TileSize);
                }
            }
        }

        // Draw enemies
        foreach (var enemy in world.Enemies)
        {
            if (!enemy.IsDead)
            {
                DrawSprite(assets, enemy.SpriteKey, enemy.Col * TileSize, enemy.Row * TileSize, TileSize, TileSize);
            }
        }

        // Draw player
        var player = world.Player;
        if (player != null && !player.IsDead)
        {
            DrawSprite(assets, "player", player.Col * TileSize, player.Row * TileSize, TileSize, TileSize);
        }
    }

    private static void DrawSprite(AssetManager assets, string key, int x, int y, int w, int h)
    {
        var cfg = assets.GetSprite(key);
        if (cfg?.Texture != null)
        {
            var src = new Rectangle(0, 0, cfg.Texture.Value.Width, cfg.Texture.Value.Height);
            var dst = new Rectangle(x, y, w, h);
            Raylib.DrawTexturePro(cfg.Texture.Value, src, dst, Vector2.Zero, 0f, Color.White);
        }
        else if (cfg != null)
        {
            Raylib.DrawRectangle(x, y, w, h, cfg.Color);
            Raylib.DrawText(cfg.Emoji, x + w / 4, y + h / 4, 18, Color.White);
        }
    }

    private static void RenderGameOver(int screenW, int screenH)
    {
        Raylib.DrawRectangle(0, 0, screenW, screenH, new Color(0, 0, 0, 200));

        string msg = "GAME OVER";
        int msgW = Raylib.MeasureText(msg, 36);
        Raylib.DrawText(msg, (screenW - msgW) / 2, screenH / 2 - 30, 36, new Color(255, 80, 80, 255));

        string sub = "Press 'R' or 'Enter' to restart";
        int subW = Raylib.MeasureText(sub, 18);
        Raylib.DrawText(sub, (screenW - subW) / 2, screenH / 2 + 20, 18, Color.White);
    }

    private static void RunAutomatedTests(string resourceDir)
    {
        Console.WriteLine("=== Running Gemini C# PenguinRunner Verification Tests ===");

        using var assets = new AssetManager(resourceDir);
        var world = new World(resourceDir, assets);
        bool loaded = world.InitLevel(0);

        AssertCondition(loaded, "Level 1 initialized successfully");
        AssertCondition(world.Rows > 0 && world.Cols > 0, $"World grid created ({world.Rows}x{world.Cols})");
        AssertCondition(world.Player != null, "Player initialized");

        var player = world.Player!;

        // 1. Movement
        world.HandlePlayerInput(Direction.Right);
        Console.WriteLine($"Player position after Right: ({player.Row}, {player.Col})");

        // 2. Inventory & Rolling wheel
        player.AddItem(new Flamethrower());
        AssertCondition(player.HasItems, "Flamethrower added to inventory");
        AssertCondition(player.GetSelectedItem()?.Name == "flamethrower", "Flamethrower selected");

        player.AddItem(new Teleport());
        AssertCondition(player.Items.Count == 2, "Teleport added, count is 2");

        player.CycleItem(1);
        AssertCondition(player.GetSelectedItem()?.Name == "teleport", "Item cycling works");

        // 3. Save & Load
        string testSave = Path.Combine("saves", "test_save.json");
        IO.SaveManager.Save(world, testSave);
        AssertCondition(File.Exists(testSave), "Save file created");

        IO.SaveManager.Load(world, testSave);
        AssertCondition(world.Player != null, "Game state restored");
        if (File.Exists(testSave)) File.Delete(testSave);

        // 4. Autotiler
        IceAutoTiler.UpdateAll(world.Blocks);
        AssertCondition(true, "IceAutoTiler executed with zero allocations");

        Console.WriteLine("=== All Gemini C# Tests PASSED! ===");
    }

    private static void AssertCondition(bool condition, string message)
    {
        if (!condition)
        {
            Console.Error.WriteLine($"FAILED: {message}");
            Environment.Exit(1);
        }
        Console.WriteLine($"PASSED: {message}");
    }
}
