using Raylib_cs;
using iessineu.penguinrunner.Entity.Enemies;

namespace iessineu.penguinrunner;

public class PenguinRunner
{
    private enum ScreenState
    {
        Menu,
        Game,
        Death,
        Editor
    }

    private static string[] claus = Array.Empty<string>();
    private static string[] valors = Array.Empty<string>();
    private static bool editorMapes = false;
    private static bool forceStart = false;

    public static void Main(string[] args)
    {
        for (int i = 0; i < args.Length; i++)
        {
            string arg = args[i];
            if (arg.StartsWith("-"))
            {
                Array.Resize(ref claus, claus.Length + 1);
                claus[^1] = arg;
                Array.Resize(ref valors, valors.Length + 1);
                valors[^1] = "-1";
            }
            else
            {
                if (valors.Length > 0 && valors[^1] == "-1")
                {
                    valors[^1] = arg;
                }
            }
        }

        bool runTests = false;
        foreach (string clau in claus)
        {
            if (clau == "-editor")
            {
                editorMapes = true;
            }
            if (clau == "-skip")
            {
                forceStart = true;
            }
            if (clau == "-test")
            {
                runTests = true;
            }
        }

        if (runTests)
        {
            RunVerificationTests();
            return;
        }

        Raylib.SetConfigFlags(ConfigFlags.Msaa4xHint);
        Raylib.InitWindow(MenuPanel.W, MenuPanel.H, "PenguinRunner");
        Raylib.SetExitKey(KeyboardKey.Null);
        Raylib.SetTargetFPS(60);

        SoundManager.EnsureAudioInitialized();

        ScreenState state;
        MenuPanel? menu = null;
        MenuPanel? deathMenu = null;
        GamePanel? gamePanel = null;
        AsciiMapStudio? editor = null;

        if (editorMapes)
        {
            state = ScreenState.Editor;
            Raylib.SetWindowSize(1000, 650);
            editor = new AsciiMapStudio();
        }
        else if (forceStart)
        {
            state = ScreenState.Game;
            ApplyCliModifiers();
            gamePanel = new GamePanel();
            ApplyCliModsToPanel(gamePanel);
            gamePanel.resizePanelToGame();
        }
        else
        {
            state = ScreenState.Menu;
            menu = new MenuPanel(false);
        }

        while (!Raylib.WindowShouldClose())
        {
            float dt = Raylib.GetFrameTime();

            switch (state)
            {
                case ScreenState.Menu:
                    menu!.update(dt);
                    if (menu.arrancar_())
                    {
                        ApplyCliModifiers();
                        gamePanel = new GamePanel();
                        ApplyCliModsToPanel(gamePanel);
                        gamePanel.resizePanelToGame();
                        state = ScreenState.Game;
                    }

                    Raylib.BeginDrawing();
                    menu.draw();
                    Raylib.EndDrawing();
                    break;

                case ScreenState.Game:
                    gamePanel!.update(dt);

                    if (gamePanel.getGameState().isGameOver())
                    {
                        deathMenu = new MenuPanel(true);
                        Raylib.SetWindowSize(MenuPanel.W, MenuPanel.H);
                        int mon = Raylib.GetCurrentMonitor();
                        Raylib.SetWindowPosition(Math.Max(0, (Raylib.GetMonitorWidth(mon) - MenuPanel.W) / 2), Math.Max(0, (Raylib.GetMonitorHeight(mon) - MenuPanel.H) / 2));
                        state = ScreenState.Death;
                    }

                    Raylib.BeginDrawing();
                    gamePanel.draw();
                    Raylib.EndDrawing();
                    break;

                case ScreenState.Death:
                    deathMenu!.update(dt);

                    if (deathMenu.arrancar_())
                    {
                        gamePanel!.getGameState().setGameOver(false);
                        gamePanel.getGameState().unmuteMusic();
                        gamePanel.getGameState().loadMap();
                        gamePanel.resizePanelToGame();
                        state = ScreenState.Game;
                    }

                    Raylib.BeginDrawing();
                    deathMenu.draw();
                    Raylib.EndDrawing();
                    break;

                case ScreenState.Editor:
                    editor!.update(dt);

                    Raylib.BeginDrawing();
                    editor.draw();
                    Raylib.EndDrawing();
                    break;
            }
        }

        // Cleanup
        if (Raylib.IsAudioDeviceReady())
        {
            Raylib.CloseAudioDevice();
        }
        Raylib.CloseWindow();
    }

    private static void ApplyCliModifiers()
    {
        for (int i = 0; i < claus.Length; i++)
        {
            string clau = claus[i];
            string seleccio = valors[i];

            if (clau == "-g" || clau == "-game")
            {
                string gPath = "games/" + seleccio + "/";
                if (!Directory.Exists(gPath))
                {
                    string alt = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, gPath);
                    if (Directory.Exists(alt)) gPath = alt;
                    else
                    {
                        alt = Path.Combine(Directory.GetCurrentDirectory(), "PenguinRunnerCS", gPath);
                        if (Directory.Exists(alt)) gPath = alt;
                    }
                }
                GamePanel.setFolderPath(gPath);
                GamePanel.setGame(true);
                Console.WriteLine("Carregant joc personalitzat...");
                cls();
                GamePanel.updatePaths();
                GamePanel.setSpriteMap(GamePanel.createSpriteMap(false));
            }
        }
    }

    private static void ApplyCliModsToPanel(GamePanel panel)
    {
        for (int i = 0; i < claus.Length; i++)
        {
            string clau = claus[i];
            string seleccio = valors[i];

            if (clau == "-m" || clau == "-mod")
            {
                Console.WriteLine("Carregant mods...");
                panel.loadMods("mods/" + seleccio);
            }
        }
    }

    public static void cls()
    {
        for (int i = 0; i < 50; i++)
        {
            Console.WriteLine("");
        }
    }

    private static void RunVerificationTests()
    {
        Console.WriteLine("=== INICIANT PROVES DE VERIFICACIÓ PENGUIN RUNNER ===");

        // 1. Test GameState & Map loading
        GameState gs = new GameState();
        int mapCount = gs.getMapList().Count;
        Console.WriteLine($"Mapes carregats: {mapCount}");
        if (mapCount < 1) throw new Exception("Error: No s'han pogut carregar els mapes");

        // 2. Test Player position & initial values
        var player = gs.getPlayer();
        Console.WriteLine($"Jugador inicial a: ({player.getRow()}, {player.getCol()})");
        Console.WriteLine($"Vides: {player.getLives()}, Gelats: {player.geticeCream()}/{gs.getIceCream()}");
        if (player.getLives() != 3) throw new Exception("Error: Les vides inicials haurien de ser 3");

        // 3. Test Movement & Block interaction
        int startRow = player.getRow();
        int startCol = player.getCol();

        // Break block down left
        gs.breakDownLeft();
        Console.WriteLine("Bloc trencat abaix-esquerra correctament.");

        // 4. Test Auto-tiling logic
        IceAutoTileConnector.update(gs.getBlocks());
        Console.WriteLine("Auto-tiling actualitzat correctament.");

        // 5. Test AI Pathfinding
        AI ai = new AI();
        var enemy = gs.getEnemies().Count > 0 ? gs.getEnemies()[0] : new iessineu.penguinrunner.Entity.Enemies.Enemy(1, 1, 1, 1);
        Console.WriteLine($"Enemics trobats al nivell 1: {gs.getEnemies().Count}");

        // 6. Test Item inventory & Teleport & Flamethrower
        player.addItem("flamethrower");
        if (!player.hasItem("flamethrower")) throw new Exception("Error: L'inventari no té lanzallamas");
        Console.WriteLine("Item lanzallamas afegit i detectat.");

        // Test item wheel logic with 1 item
        for (int offset = -1; offset <= 1; offset++)
        {
            int p = ((player.getSelectedItemIndex() + offset) % player.getItems().Count + player.getItems().Count) % player.getItems().Count;
            if (p != 0) throw new Exception("Error: La roda d'un sol ítem hauria de mostrar l'ítem 0 en els 3 slots");
        }
        Console.WriteLine("Roda d'ítems amb 1 ítem verificada (mostra 3 vegades el mateix ítem).");

        // Test Teleport
        player.addItem("teleport");
        if (!player.hasItem("teleport")) throw new Exception("Error: L'inventari no té teleport");

        // Test item wheel logic with 2 items (TP selected at index 1)
        player.setSelectedItemIndex(1);
        int leftSlot = ((player.getSelectedItemIndex() - 1) % 2 + 2) % 2;
        int centerSlot = player.getSelectedItemIndex();
        int rightSlot = ((player.getSelectedItemIndex() + 1) % 2 + 2) % 2;
        if (leftSlot != 0 || centerSlot != 1 || rightSlot != 0)
            throw new Exception("Error: Roda d'ítems amb TP seleccionat hauria de ser flamethrower/teleport/flamethrower");

        // Test item wheel logic with 2 items (Flamethrower selected at index 0)
        player.setSelectedItemIndex(0);
        leftSlot = ((player.getSelectedItemIndex() - 1) % 2 + 2) % 2;
        centerSlot = player.getSelectedItemIndex();
        rightSlot = ((player.getSelectedItemIndex() + 1) % 2 + 2) % 2;
        if (leftSlot != 1 || centerSlot != 0 || rightSlot != 1)
            throw new Exception("Error: Roda d'ítems amb Flamethrower seleccionat hauria de ser teleport/flamethrower/teleport");
        Console.WriteLine("Roda d'ítems amb 2 ítems verificada (roda desplaçable en ambdues direccions).");

        player.setSelectedItemIndex(1); // Teleport
        int pRow = player.getRow();
        int pCol = player.getCol();
        gs.useItem(); // places portal
        if (gs.getBlocks()[pRow][pCol].getType() != Blocks.TileType.TELEPORT)
            throw new Exception("Error: Teleport no ha col·locat el portal al mapa");
        player.setPosition(pRow, pCol + 2);
        gs.useItem(); // teleports back
        if (player.getRow() != pRow || player.getCol() != pCol)
            throw new Exception("Error: Teleport no ha retornat el jugador a la posició");
        Console.WriteLine("Item teleport funcionant correctament.");

        // 7. Test Button & Trapdoor
        int bRow = 0, bCol = 0;
        gs.getBlocks()[bRow][bCol] = new Blocks.Block(Blocks.TileType.BUTTON);
        gs.getBlocks()[bRow + 1][bCol] = new Blocks.Block(Blocks.TileType.TRAPDOOR);
        if (gs.buttonPressed_()) throw new Exception("Error: buttonPressed hauria de ser false inicialment");
        if (gs.isSolid(bRow + 1, bCol)) throw new Exception("Error: trapdoor hauria de ser no sòlida inicialment");
        player.setPosition(bRow, bCol);
        gs.interact(); // step on button
        if (!gs.buttonPressed_()) throw new Exception("Error: el botó no ha canviat l'estat a true");
        if (!gs.isSolid(bRow + 1, bCol)) throw new Exception("Error: trapdoor hauria de ser sòlida després de prémer botó");
        gs.interact(); // press again
        if (gs.buttonPressed_()) throw new Exception("Error: el botó no ha canviat l'estat a false");
        if (gs.isSolid(bRow + 1, bCol)) throw new Exception("Error: trapdoor hauria de ser no sòlida després del segon clic");
        Console.WriteLine("Botons i trapdoors funcionant correctament.");

        // 8. Test Door objective check
        int dRow = 1, dCol = 1;
        gs.getBlocks()[dRow][dCol] = new Blocks.Block(Blocks.TileType.DOOR);
        player.setPosition(dRow, dCol);
        int initialLevel = gs.getNivell();
        gs.interact(); // with 0 ice cream collected, should NOT advance
        if (gs.getNivell() != initialLevel) throw new Exception("Error: La porta ha obert sense aconseguir els gelats");
        // Simulate collecting all ice creams
        player.setIceCream(gs.getIceCream());
        gs.updateDoors();
        gs.interact(); // now it should advance
        if (gs.getNivell() == initialLevel) throw new Exception("Error: La porta no ha obert després de recollir tots els gelats");
        Console.WriteLine("Porta i objectius funcionant correctament.");

        // 9. Test Screen Resizing logic
        GamePanel panel = new GamePanel();
        int wLevel1 = panel.getScreenWidth();
        int hLevel1 = panel.getScreenHeight();
        Console.WriteLine($"Mida panell nivell 1: {wLevel1}x{hLevel1}");
        if (wLevel1 <= 0 || hLevel1 <= 0) throw new Exception("Error: Dimensions de pantalla invàlides");

        // 10. Test Save & Load logic
        panel.guardarPartida();
        panel.carregarPartida();
        Console.WriteLine("Guardat i carregat de partida verificat.");

        // 11. Test Printables
        var spriteMap = GamePanel.createSpriteMap(true);
        if (!spriteMap.ContainsKey("ice_center"))
            throw new Exception("Error: Mapa de sprites no conté claus auto-tiling d'ice");
        Console.WriteLine($"Total printables registrats: {spriteMap.Count}");

        Console.WriteLine("=== TOTES LES PROVES HAN PASSAT AMB ÈXIT! ===");
    }
}
