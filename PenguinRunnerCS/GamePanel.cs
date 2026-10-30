using System.Numerics;
using System.Text.Json;
using iessineu.penguinrunner.Blocks;
using iessineu.penguinrunner.Entity;
using iessineu.penguinrunner.Entity.Enemies;
using iessineu.penguinrunner.Entity.Items;
using iessineu.penguinrunner.Movement;
using Raylib_cs;

namespace iessineu.penguinrunner;

public class GamePanel
{
    private static bool game = false;
    public const int TILE_SIZE = 43;
    private const int HUD_HEIGHT = 100;

    private static string folderPath = "resources/";
    private static string musicPath = folderPath + "music.wav";
    private static string printablesPath = folderPath + "printables.json";
    private static string emojiFontPath = folderPath + "emoji.ttf";
    private static string textFontPath = folderPath + "font.ttf";

    private static Dictionary<string, List<string>> spriteMap = createSpriteMap(!hasGame());

    private Font textFont;
    private Font emojiFont;
    private bool fontsLoaded = false;

    private GameState gameState;
    private Texture2D? selectedArrowIcon = null;

    private bool isPaused = false;
    private PauseMenuDialog pauseMenu;

    public GamePanel()
    {
        gameState = new GameState();
        gameState.playMusic(!hasGame());
        loadFonts(!hasGame());
        Printable.setFont(emojiFont);

        // Load selected arrow icon
        string arrowPath = Path.Combine("resources", "sprites", "selected.png");
        if (!File.Exists(arrowPath))
        {
            arrowPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "resources", "sprites", "selected.png");
        }
        if (File.Exists(arrowPath))
        {
            selectedArrowIcon = Printable.LoadCachedTexture(arrowPath);
        }

        pauseMenu = new PauseMenuDialog(gameState);
    }

    private void playTurn(Direction direction)
    {
        gameState.takeTurn(direction);
    }

    private void loadFonts(bool fromResource)
    {
        string emojiPath = fromResource
            ? Path.Combine("resources", "emoji.ttf")
            : emojiFontPath;
        string textPath = fromResource
            ? Path.Combine("resources", "font.ttf")
            : textFontPath;

        if (!File.Exists(emojiPath))
        {
            string alt = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, emojiPath);
            if (File.Exists(alt)) emojiPath = alt;
            else
            {
                alt = Path.Combine(Directory.GetCurrentDirectory(), "PenguinRunnerCS", emojiPath);
                if (File.Exists(alt)) emojiPath = alt;
            }
        }

        if (!File.Exists(textPath))
        {
            string alt = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, textPath);
            if (File.Exists(alt)) textPath = alt;
            else
            {
                alt = Path.Combine(Directory.GetCurrentDirectory(), "PenguinRunnerCS", textPath);
                if (File.Exists(alt)) textPath = alt;
            }
        }

        Console.Write("Carregant fonts: ");
        try
        {
            if (File.Exists(emojiPath))
            {
                emojiFont = Raylib.LoadFontEx(emojiPath, 30, null, 0);
            }
            else
            {
                emojiFont = Raylib.GetFontDefault();
            }

            if (File.Exists(textPath))
            {
                textFont = Raylib.LoadFontEx(textPath, 16, null, 0);
            }
            else
            {
                textFont = Raylib.GetFontDefault();
            }
            fontsLoaded = true;
        }
        catch (Exception ex)
        {
            Console.WriteLine("Error obrint alguna de les fonts: " + ex.Message);
            emojiFont = Raylib.GetFontDefault();
            textFont = Raylib.GetFontDefault();
        }
    }

    public void resizePanelToGame()
    {
        int targetW = getScreenWidth();
        int targetH = getScreenHeight();
        if (targetW > 0 && targetH > 0 && (Raylib.GetScreenWidth() != targetW || Raylib.GetScreenHeight() != targetH))
        {
            Raylib.SetWindowSize(targetW, targetH);
            int monitor = Raylib.GetCurrentMonitor();
            int monW = Raylib.GetMonitorWidth(monitor);
            int monH = Raylib.GetMonitorHeight(monitor);
            Raylib.SetWindowPosition(Math.Max(0, (monW - targetW) / 2), Math.Max(0, (monH - targetH) / 2));
        }
    }

    public void ResizePanelToGame() => resizePanelToGame();

    public void update(float dt)
    {
        if (isPaused)
        {
            pauseMenu.update(dt);
            if (!pauseMenu.isOpen())
            {
                isPaused = false;
                int selectedOption = pauseMenu.getSelectedOption();
                switch (selectedOption)
                {
                    case PauseMenuDialog.RESUME:
                        gameState.unmuteMusic();
                        break;
                    case PauseMenuDialog.SAVE:
                        gameState.unmuteMusic();
                        guardarPartida();
                        break;
                    case PauseMenuDialog.LOAD:
                        gameState.unmuteMusic();
                        carregarPartida();
                        break;
                    case PauseMenuDialog.EXIT:
                        Environment.Exit(0);
                        break;
                    default:
                        gameState.unmuteMusic();
                        break;
                }
            }
            return;
        }

        resizePanelToGame();
        gameState.update(dt);
        handleInput();
    }

    public void Update(float dt) => update(dt);

    private void handleInput()
    {
        Direction? direction = null;
        bool anyKeyPressed = false;

        if (Raylib.IsKeyPressed(KeyboardKey.Up) || Raylib.IsKeyPressed(KeyboardKey.W) || Raylib.IsKeyPressedRepeat(KeyboardKey.Up) || Raylib.IsKeyPressedRepeat(KeyboardKey.W))
        {
            direction = Direction.UP;
            playTurn(direction);
            anyKeyPressed = true;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Down) || Raylib.IsKeyPressed(KeyboardKey.S) || Raylib.IsKeyPressedRepeat(KeyboardKey.Down) || Raylib.IsKeyPressedRepeat(KeyboardKey.S))
        {
            direction = Direction.DOWN;
            playTurn(direction);
            anyKeyPressed = true;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Left) || Raylib.IsKeyPressed(KeyboardKey.A) || Raylib.IsKeyPressedRepeat(KeyboardKey.Left) || Raylib.IsKeyPressedRepeat(KeyboardKey.A))
        {
            direction = Direction.LEFT;
            playTurn(direction);
            anyKeyPressed = true;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Right) || Raylib.IsKeyPressed(KeyboardKey.D) || Raylib.IsKeyPressedRepeat(KeyboardKey.Right) || Raylib.IsKeyPressedRepeat(KeyboardKey.D))
        {
            direction = Direction.RIGHT;
            playTurn(direction);
            anyKeyPressed = true;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Space))
        {
            gameState.updateLogic();
            anyKeyPressed = true;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Q))
        {
            gameState.breakDownLeft();
            anyKeyPressed = true;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.E))
        {
            gameState.breakDownRight();
            anyKeyPressed = true;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.F))
        {
            gameState.useItem();
            anyKeyPressed = true;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.X))
        {
            gameState.changeItem(false);
            anyKeyPressed = true;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Z))
        {
            gameState.changeItem(true);
            anyKeyPressed = true;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.R))
        {
            gameState.playerDied(true);
            anyKeyPressed = true;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Escape))
        {
            menuPausa();
            return;
        }

        if (anyKeyPressed)
        {
            if (!gameState.isGameOver())
            {
                if (gameState.isMuted_())
                {
                    gameState.unmuteMusic();
                }
                gameState.interact();
                resizePanelToGame();
            }
        }
    }

    public void menuPausa()
    {
        isPaused = true;
        pauseMenu.open(gameState.getGameVolume());
    }

    public void MenuPausa() => menuPausa();

    public static string getSavesFolderPath()
    {
        string projectSaves = Path.Combine(Directory.GetCurrentDirectory(), "PenguinRunnerCS", "saves");
        if (Directory.Exists(Path.Combine(Directory.GetCurrentDirectory(), "PenguinRunnerCS")))
        {
            return projectSaves;
        }
        string localSaves = Path.Combine(Directory.GetCurrentDirectory(), "saves");
        if (Directory.Exists(localSaves))
        {
            return localSaves;
        }
        return Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "saves");
    }

    public void guardarPartida()
    {
        try
        {
            string savesFolder = getSavesFolderPath();
            if (!Directory.Exists(savesFolder))
            {
                Directory.CreateDirectory(savesFolder);
            }

            string[] saves = Directory.GetFiles(savesFolder, "*.json");
            string nomArxiu = "partidaGuardada" + saves.Length + ".json";
            string fullPath = Path.Combine(savesFolder, nomArxiu);

            // Save basic level and player state
            var stateData = new
            {
                Level = gameState.getNivell(),
                Lives = gameState.getPlayer().getLives(),
                IceCream = gameState.getPlayer().geticeCream(),
                PlayerRow = gameState.getPlayer().getRow(),
                PlayerCol = gameState.getPlayer().getCol()
            };

            File.WriteAllText(fullPath, JsonSerializer.Serialize(stateData, new JsonSerializerOptions { WriteIndented = true }));
            Console.WriteLine("Partida guardada a: " + fullPath);
        }
        catch (Exception ex)
        {
            Console.WriteLine("Error guardant partida: " + ex.Message);
        }
    }

    public void GuardarPartida() => guardarPartida();

    public void carregarPartida()
    {
        try
        {
            string savesFolder = getSavesFolderPath();
            if (!Directory.Exists(savesFolder))
            {
                Console.WriteLine("No existeix la carpeta saves.");
                return;
            }

            string[] saves = Directory.GetFiles(savesFolder, "*.json");
            if (saves.Length == 0)
            {
                Console.WriteLine("No hi ha cap partida guardada.");
                return;
            }

            string latestSave = saves[^1];
            string json = File.ReadAllText(latestSave);
            using var doc = JsonDocument.Parse(json);
            var root = doc.RootElement;

            int level = root.GetProperty("Level").GetInt32();
            int lives = root.GetProperty("Lives").GetInt32();
            int iceCream = root.GetProperty("IceCream").GetInt32();
            int pRow = root.GetProperty("PlayerRow").GetInt32();
            int pCol = root.GetProperty("PlayerCol").GetInt32();

            while (gameState.getNivell() != level && gameState.getMapList().Count > level)
            {
                gameState.interact();
            }

            gameState.getPlayer().setLives(lives);
            gameState.getPlayer().setIceCream(iceCream);
            gameState.getPlayer().setPosition(pRow, pCol);

            Console.WriteLine("Partida carregada: " + latestSave);
            gameState.reloadSprites();
            gameState.reloadAudioAfterLoad();
        }
        catch (Exception ex)
        {
            Console.WriteLine("Error carregant partida: " + ex.Message);
        }
    }

    public void CarregarPartida() => carregarPartida();

    public void draw()
    {
        Raylib.ClearBackground(Color.Black);

        drawMap();
        drawEnemies();
        drawPlayer();
        drawHUD();

        if (isPaused)
        {
            pauseMenu.draw();
        }
    }

    public void Draw() => draw();

    private void drawMap()
    {
        IceAutoTileConnector.update(gameState.getBlocks());

        for (int row = 0; row < gameState.getRows(); row++)
        {
            for (int col = 0; col < gameState.getCols(); col++)
            {
                Block b = gameState.getBlocks()[row][col];
                if (b != null)
                {
                    if (b.isDoor())
                    {
                        if (gameState.checkObjective())
                        {
                            b.setType(TileType.DOOR);
                            b.setPrintables();
                        }
                    }
                    if (b.isTrapdoor())
                    {
                        if (gameState.buttonPressed_())
                        {
                            b.setType(TileType.TRAPDOOR);
                            b.setSolid(true);
                        }
                        else
                        {
                            b.setType(TileType.BLANK);
                            b.setSolid(false);
                        }
                        b.setPrintables();
                    }
                    b.draw(row, col);
                }
            }
        }
    }

    private void drawPlayer()
    {
        Player p = gameState.getPlayer();
        if (p != null)
        {
            p.draw(p.getRow(), p.getCol());
        }
    }

    private void drawEnemies()
    {
        foreach (Enemy enemy in gameState.getEnemies())
        {
            if (!enemy.isDead())
            {
                enemy.draw(enemy.getRow(), enemy.getCol());
            }
        }
    }

    private void drawHUD()
    {
        int hudY = gameState.getRows() * TILE_SIZE;
        int screenWidth = gameState.getCols() * TILE_SIZE;

        // Fons del HUD
        Raylib.DrawRectangle(0, hudY, screenWidth, HUD_HEIGHT, new Color(18, 18, 28, 255));

        // Línia superior
        Raylib.DrawLine(0, hudY, screenWidth, hudY, new Color(90, 130, 160, 255));

        Player player = gameState.getPlayer();
        Block icecream = new Block(TileType.ICECREAM);

        // Vides (dibuixam el jugador tantes vegades com vides)
        for (int i = 0; i < player.getLives(); i++)
        {
            player.draw(gameState.getRows(), 1 + i);
        }

        // Gelat
        icecream.draw(gameState.getRows(), 5);

        // Comptador de gelats
        string iceCreamText = player.geticeCream() + "/ " + gameState.getIceCream();
        Raylib.DrawTextEx(fontsLoaded ? textFont : Raylib.GetFontDefault(), iceCreamText, new Vector2(6 * TILE_SIZE, hudY + 12), 18, 1, Color.White);

        // Nivell
        string nivellText = "Nivell: " + (gameState.getNivell() + 1);
        Raylib.DrawTextEx(fontsLoaded ? textFont : Raylib.GetFontDefault(), nivellText, new Vector2(8 * TILE_SIZE, hudY + 12), 18, 1, Color.White);

        // Controls help
        string controls = "<- ^ v -> / WASD: moure, Q/E Rompre blocs, Z/X Canviar objecte, F: Interactuar, R: Reiniciar nivell ESC: Pausa";
        Raylib.DrawTextEx(fontsLoaded ? textFont : Raylib.GetFontDefault(), controls, new Vector2(20, hudY + 60), 12, 1, new Color(190, 210, 230, 255));

        // Items
        if (player.hasItems())
        {
            var items = player.getItems();
            int sel = player.getSelectedItemIndex();

            // 3-slot item wheel:
            // offset -1 -> Left slot (col = getCols() - 5): previous item in wheel
            // offset  0 -> Center slot (col = getCols() - 4): currently selected item
            // offset +1 -> Right slot (col = getCols() - 3): next item in wheel
            for (int offset = -1; offset <= 1; offset++)
            {
                int pointer = ((sel + offset) % items.Count + items.Count) % items.Count;
                int col = gameState.getCols() - 4 + offset;

                Item it = items[pointer];
                Block? itemBlock = null;
                switch (it.getName().ToLowerInvariant())
                {
                    case "flamethrower":
                        itemBlock = new Block(TileType.FLAMETHROWER);
                        Flamethrower f = (Flamethrower)it;
                        string usos = "(" + f.getUsesLeft() + ")";
                        Raylib.DrawTextEx(fontsLoaded ? textFont : Raylib.GetFontDefault(), usos, new Vector2(col * TILE_SIZE + 4, hudY + 44), 14, 1, Color.White);
                        break;
                    case "teleport":
                        itemBlock = new Block(TileType.TELEPORT);
                        break;
                }
                if (itemBlock != null)
                {
                    itemBlock.draw(gameState.getRows(), col);
                }
            }

            // Arrow selector (selection frame on the center slot)
            if (!selectedArrowIcon.HasValue || selectedArrowIcon.Value.Id <= 0)
            {
                selectedArrowIcon = Printable.LoadCachedTexture("selected.png");
            }
            if (selectedArrowIcon.HasValue && selectedArrowIcon.Value.Id > 0)
            {
                float ax = (gameState.getCols() - 4) * TILE_SIZE;
                float ay = gameState.getRows() * TILE_SIZE;
                Raylib.DrawTexturePro(selectedArrowIcon.Value,
                    new Rectangle(0, 0, selectedArrowIcon.Value.Width, selectedArrowIcon.Value.Height),
                    new Rectangle(ax, ay, TILE_SIZE, TILE_SIZE),
                    Vector2.Zero, 0f, Color.White);
            }
        }
    }

    public static Dictionary<string, List<string>> createSpriteMap(bool fromResource)
    {
        string path = fromResource
            ? Path.Combine("resources", "printables.json")
            : printablesPath;

        if (!File.Exists(path))
        {
            string[] candidates = new[]
            {
                Path.Combine(AppDomain.CurrentDomain.BaseDirectory, path),
                Path.Combine(Directory.GetCurrentDirectory(), "PenguinRunnerCS", path),
                Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "resources", "printables.json"),
                Path.Combine(Directory.GetCurrentDirectory(), "resources", "printables.json"),
                Path.Combine(Directory.GetCurrentDirectory(), "PenguinRunnerCS", "resources", "printables.json")
            };
            foreach (var cand in candidates)
            {
                if (File.Exists(cand))
                {
                    path = cand;
                    break;
                }
            }
        }

        Console.Write("Carregant Printables: ");
        if (fromResource)
        {
            Console.WriteLine("Carregant Recurs (" + path + ")");
        }
        else
        {
            Console.WriteLine("Carregant Fitxer (" + path + ")");
        }

        var map = new Dictionary<string, List<string>>();

        if (!File.Exists(path))
        {
            return map;
        }

        try
        {
            string json = File.ReadAllText(path);
            using var doc = JsonDocument.Parse(json);
            foreach (var elem in doc.RootElement.EnumerateArray())
            {
                string type = elem.TryGetProperty("type", out var tp) ? tp.GetString() ?? "" : "";
                string sprite = elem.TryGetProperty("sprite", out var sp) ? sp.GetString() ?? "" : "";
                string color = elem.TryGetProperty("color", out var cl) ? cl.GetString() ?? "" : "";
                string filename = elem.TryGetProperty("filename", out var fn) ? fn.GetString() ?? "" : "";

                List<string> atributs = new() { sprite, color, filename };
                map[type] = atributs;
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine("Error parsing printables: " + ex.Message);
        }

        return map;
    }

    public void loadMods(string path)
    {
        string modsFile = Path.Combine(path, "mods.json");
        if (!File.Exists(modsFile))
        {
            Console.WriteLine("No s'ha trobat " + modsFile);
            return;
        }

        try
        {
            string json = File.ReadAllText(modsFile);
            using var doc = JsonDocument.Parse(json);
            foreach (var mod in doc.RootElement.EnumerateArray())
            {
                string modType = mod.GetProperty("modtype").GetString() ?? "";
                var modValue = mod.GetProperty("modvalue");
                gameState.addMod(modType, modValue, path);
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine("Error carregant mods: " + ex.Message);
        }
    }

    public GameState getGameState() => gameState;
    public GameState GetGameState() => gameState;

    public int getScreenWidth() => gameState.getCols() * TILE_SIZE;
    public int getScreenHeight() => gameState.getRows() * TILE_SIZE + HUD_HEIGHT;

    public static string getPrintablesPath() => printablesPath;
    public static void setPrintablesPath(string path) => printablesPath = path;

    public static string getEmojiFontPath() => emojiFontPath;
    public static void setEmojiFontPath(string path) => emojiFontPath = path;

    public static string getTextFontPath() => textFontPath;
    public static void setTextFontPath(string path) => textFontPath = path;

    public static string getMusicPath() => musicPath;
    public static void setMusicPath(string path) => musicPath = path;

    public static string getFolderPath() => folderPath;
    public static void setFolderPath(string path)
    {
        folderPath = path.EndsWith('/') || path.EndsWith('\\') ? path : path + "/";
    }

    public static Dictionary<string, List<string>> getSpriteMap() => spriteMap;
    public static void setSpriteMap(Dictionary<string, List<string>> map) => spriteMap = map;

    public static bool hasGame() => game;
    public static void setGame(bool g) => game = g;

    public static void updatePaths()
    {
        musicPath = folderPath + "music.wav";
        printablesPath = folderPath + "printables.json";
        emojiFontPath = folderPath + "emoji.ttf";
        textFontPath = folderPath + "font.ttf";
        Console.WriteLine("S'han actualitzat les rutes.");
    }
}

