using System.Numerics;
using System.Text.Json;
using Raylib_cs;

namespace iessineu.penguinrunner;

public class AsciiMapStudio
{
    private class MapEntry
    {
        public string type { get; set; } = "map";
        public int level { get; set; } = 1;
        public List<string> view { get; set; } = new();

        public MapEntry(string type, int level, List<string> view)
        {
            this.type = type;
            this.level = level;
            this.view = view;
        }
    }

    private string currentFile = Path.Combine("resources", "maps.json");
    private List<MapEntry> maps = new();
    private int selectedMapIndex = 0;

    private int gridCols = 31;
    private int gridRows = 9;
    private char[,] grid = new char[9, 31];

    private char paintChar = '#';
    private const int CELL_SIZE = 26;
    private const int GRID_START_X = 20;
    private const int GRID_START_Y = 160;

    private static readonly char[] QUICK_CHARS = "# .-HGPDEFWTASBbgCn".ToCharArray();

    public AsciiMapStudio()
    {
        if (!File.Exists(currentFile))
        {
            string alt = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, currentFile);
            if (File.Exists(alt)) currentFile = alt;
            else
            {
                alt = Path.Combine(Directory.GetCurrentDirectory(), "PenguinRunnerCS", currentFile);
                if (File.Exists(alt)) currentFile = alt;
            }
        }

        if (File.Exists(currentFile))
        {
            openFile(currentFile);
        }
        else
        {
            newMap();
        }
    }

    public void openFile(string path)
    {
        try
        {
            if (!File.Exists(path))
            {
                string alt = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, path);
                if (File.Exists(alt)) path = alt;
            }

            if (File.Exists(path))
            {
                string json = File.ReadAllText(path);
                using var doc = JsonDocument.Parse(json);
                maps.Clear();
                foreach (var elem in doc.RootElement.EnumerateArray())
                {
                    string tp = elem.GetProperty("type").GetString() ?? "map";
                    int lvl = elem.GetProperty("level").GetInt32();
                    List<string> vw = new();
                    foreach (var l in elem.GetProperty("view").EnumerateArray())
                    {
                        vw.Add(l.GetString() ?? "");
                    }
                    maps.Add(new MapEntry(tp, lvl, vw));
                }

                currentFile = path;
                selectedMapIndex = 0;
                loadMapToGrid();
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine("Error obrint mapes: " + ex.Message);
            newMap();
        }
    }

    public void save()
    {
        try
        {
            saveGridToCurrentMap();
            var options = new JsonSerializerOptions { WriteIndented = true };
            string json = JsonSerializer.Serialize(maps, options);
            File.WriteAllText(currentFile, json);
            Console.WriteLine("Mapes guardats a " + currentFile);
        }
        catch (Exception ex)
        {
            Console.WriteLine("Error guardant mapes: " + ex.Message);
        }
    }

    public void newMap()
    {
        saveGridToCurrentMap();
        List<string> defaultView = new()
        {
            "###############################",
            "#                             #",
            "#                             #",
            "#                             #",
            "#                             #",
            "#                             #",
            "#   P                         #",
            "#.............................#",
            "###############################"
        };
        int nextLevel = maps.Count + 1;
        maps.Add(new MapEntry("map", nextLevel, defaultView));
        selectedMapIndex = maps.Count - 1;
        loadMapToGrid();
    }

    public void duplicateMap()
    {
        if (maps.Count == 0) return;
        saveGridToCurrentMap();
        var current = maps[selectedMapIndex];
        var dupView = new List<string>(current.view);
        maps.Add(new MapEntry(current.type, maps.Count + 1, dupView));
        selectedMapIndex = maps.Count - 1;
        loadMapToGrid();
    }

    public void deleteSelectedMap()
    {
        if (maps.Count <= 1) return;
        maps.RemoveAt(selectedMapIndex);
        if (selectedMapIndex >= maps.Count)
        {
            selectedMapIndex = maps.Count - 1;
        }
        loadMapToGrid();
    }

    private void loadMapToGrid()
    {
        if (selectedMapIndex < 0 || selectedMapIndex >= maps.Count) return;
        var entry = maps[selectedMapIndex];

        gridRows = entry.view.Count > 0 ? entry.view.Count : 9;
        gridCols = entry.view.Count > 0 ? entry.view[0].Length : 31;

        grid = new char[gridRows, gridCols];
        for (int r = 0; r < gridRows; r++)
        {
            string line = r < entry.view.Count ? entry.view[r] : "";
            for (int c = 0; c < gridCols; c++)
            {
                grid[r, c] = c < line.Length ? line[c] : ' ';
            }
        }
    }

    private void saveGridToCurrentMap()
    {
        if (selectedMapIndex < 0 || selectedMapIndex >= maps.Count) return;
        var entry = maps[selectedMapIndex];
        entry.view.Clear();
        for (int r = 0; r < gridRows; r++)
        {
            char[] rowChars = new char[gridCols];
            for (int c = 0; c < gridCols; c++)
            {
                rowChars[c] = grid[r, c];
            }
            entry.view.Add(new string(rowChars));
        }
    }

    public void update(float dt)
    {
        Vector2 mousePos = Raylib.GetMousePosition();

        // Check Toolbar clicks
        // Level buttons: [<] [Level X] [>]
        if (Raylib.IsMouseButtonPressed(MouseButton.Left))
        {
            // Prev map
            if (mousePos.X >= 150 && mousePos.X <= 180 && mousePos.Y >= 15 && mousePos.Y <= 45)
            {
                if (selectedMapIndex > 0)
                {
                    saveGridToCurrentMap();
                    selectedMapIndex--;
                    loadMapToGrid();
                }
            }
            // Next map
            if (mousePos.X >= 280 && mousePos.X <= 310 && mousePos.Y >= 15 && mousePos.Y <= 45)
            {
                if (selectedMapIndex < maps.Count - 1)
                {
                    saveGridToCurrentMap();
                    selectedMapIndex++;
                    loadMapToGrid();
                }
            }
            // Nou mapa
            if (mousePos.X >= 330 && mousePos.X <= 430 && mousePos.Y >= 15 && mousePos.Y <= 45)
            {
                newMap();
            }
            // Duplicar
            if (mousePos.X >= 440 && mousePos.X <= 540 && mousePos.Y >= 15 && mousePos.Y <= 45)
            {
                duplicateMap();
            }
            // Eliminar
            if (mousePos.X >= 550 && mousePos.X <= 640 && mousePos.Y >= 15 && mousePos.Y <= 45)
            {
                deleteSelectedMap();
            }
            // Guardar
            if (mousePos.X >= 650 && mousePos.X <= 750 && mousePos.Y >= 15 && mousePos.Y <= 45)
            {
                save();
            }

            // Quick characters palette
            int charStartX = 20;
            int charY = 70;
            for (int i = 0; i < QUICK_CHARS.Length; i++)
            {
                int bx = charStartX + i * 36;
                if (mousePos.X >= bx && mousePos.X <= bx + 32 && mousePos.Y >= charY && mousePos.Y <= charY + 32)
                {
                    paintChar = QUICK_CHARS[i];
                }
            }
        }

        // Paint on grid with Left Click or Right Click (erase with space on right click)
        if (Raylib.IsMouseButtonDown(MouseButton.Left) || Raylib.IsMouseButtonDown(MouseButton.Right))
        {
            char charToPaint = Raylib.IsMouseButtonDown(MouseButton.Right) ? ' ' : paintChar;
            int col = (int)((mousePos.X - GRID_START_X) / CELL_SIZE);
            int row = (int)((mousePos.Y - GRID_START_Y) / CELL_SIZE);

            if (row >= 0 && row < gridRows && col >= 0 && col < gridCols)
            {
                grid[row, col] = charToPaint;
            }
        }
    }

    public void draw()
    {
        Raylib.ClearBackground(new Color(24, 26, 32, 255));

        // Top Toolbar
        Raylib.DrawRectangle(0, 0, Raylib.GetScreenWidth(), 120, new Color(36, 40, 50, 255));
        Raylib.DrawLine(0, 120, Raylib.GetScreenWidth(), 120, new Color(60, 68, 85, 255));

        // Map selector
        Raylib.DrawText("Mapa:", 20, 20, 20, Color.White);
        Raylib.DrawRectangle(150, 15, 30, 30, new Color(60, 70, 90, 255));
        Raylib.DrawText("<", 159, 20, 20, Color.White);

        string mapInfo = $"Nivell {selectedMapIndex + 1} / {maps.Count}";
        Raylib.DrawText(mapInfo, 190, 22, 18, Color.Yellow);

        Raylib.DrawRectangle(280, 15, 30, 30, new Color(60, 70, 90, 255));
        Raylib.DrawText(">", 291, 20, 20, Color.White);

        // Buttons
        DrawButton(330, 15, 100, 30, "Nou mapa", new Color(50, 140, 70, 255));
        DrawButton(440, 15, 100, 30, "Duplicar", new Color(60, 100, 150, 255));
        DrawButton(550, 15, 90, 30, "Eliminar", new Color(160, 60, 60, 255));
        DrawButton(650, 15, 100, 30, "Guardar", new Color(80, 140, 220, 255));

        // Quick characters
        Raylib.DrawText("Caràcters: ", 20, 76, 16, Color.LightGray);
        int charStartX = 110;
        int charY = 70;
        for (int i = 0; i < QUICK_CHARS.Length; i++)
        {
            char c = QUICK_CHARS[i];
            int bx = charStartX + i * 36;
            bool isCurrent = c == paintChar;

            Color btnCol = isCurrent ? new Color(100, 180, 255, 255) : new Color(50, 55, 70, 255);
            Raylib.DrawRectangle(bx, charY, 32, 32, btnCol);
            Raylib.DrawRectangleLines(bx, charY, 32, 32, isCurrent ? Color.White : Color.Gray);

            string s = c == ' ' ? "sp" : c.ToString();
            Raylib.DrawText(s, bx + 10, charY + 8, 16, isCurrent ? Color.Black : Color.White);
        }

        // Selected brush display
        Raylib.DrawText($"Pincell actual: '{paintChar}' (Clic esquerre = pintar, clic dret = esborrar)", 20, 130, 16, Color.Gold);

        // Draw Grid
        for (int r = 0; r < gridRows; r++)
        {
            for (int c = 0; c < gridCols; c++)
            {
                int x = GRID_START_X + c * CELL_SIZE;
                int y = GRID_START_Y + r * CELL_SIZE;

                char ch = grid[r, c];
                Color cellBg = GetCellColor(ch);

                Raylib.DrawRectangle(x, y, CELL_SIZE, CELL_SIZE, cellBg);
                Raylib.DrawRectangleLines(x, y, CELL_SIZE, CELL_SIZE, new Color(40, 45, 55, 255));

                if (ch != ' ')
                {
                    Raylib.DrawText(ch.ToString(), x + 7, y + 5, 16, Color.White);
                }
            }
        }
    }

    private void DrawButton(int x, int y, int w, int h, string text, Color col)
    {
        Raylib.DrawRectangle(x, y, w, h, col);
        Raylib.DrawRectangleLines(x, y, w, h, Color.White);
        int tw = Raylib.MeasureText(text, 14);
        Raylib.DrawText(text, x + (w - tw) / 2, y + 8, 14, Color.White);
    }

    private Color GetCellColor(char c)
    {
        return c switch
        {
            '#' => new Color(70, 70, 80, 255),
            '.' => new Color(40, 100, 160, 255),
            'H' => new Color(120, 80, 40, 255),
            '-' => new Color(110, 40, 140, 255),
            'D' => new Color(140, 60, 20, 255),
            'P' => new Color(20, 120, 50, 255),
            'E' or 'A' or 'W' or 'C' => new Color(150, 40, 40, 255),
            'G' => new Color(180, 170, 40, 255),
            'S' => new Color(90, 90, 90, 255),
            'B' or 'b' => new Color(30, 100, 120, 255),
            'F' => new Color(180, 60, 20, 255),
            'T' => new Color(80, 40, 120, 255),
            _ => new Color(16, 18, 22, 255)
        };
    }
}

