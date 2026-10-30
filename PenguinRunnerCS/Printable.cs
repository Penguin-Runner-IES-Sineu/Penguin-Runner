using System.Numerics;
using Raylib_cs;

namespace iessineu.penguinrunner;

public class Printable
{
    protected string emoji = "X";
    protected Texture2D? sprite = null;
    protected string? pendingSpritePath = null;
    protected Color color = Color.White;
    protected static Font font;
    protected static bool fontLoaded = false;

    public const int TILE_SIZE = 43;

    // Cache to prevent loading the same texture repeatedly into GPU memory
    private static readonly Dictionary<string, Texture2D> textureCache = new();

    public string getEmoji() => emoji;
    public void setEmoji(string e) => emoji = e;
    public string GetEmoji() => emoji;
    public void SetEmoji(string e) => emoji = e;

    public Texture2D? getSprite() => sprite;
    public Texture2D? GetSprite() => sprite;

    public static Texture2D? LoadCachedTexture(string path)
    {
        if (string.IsNullOrWhiteSpace(path)) return null;
        if (!Raylib.IsWindowReady()) return null;

        string filename = Path.GetFileName(path);
        string[] candidates = new[]
        {
            path,
            Path.Combine(AppDomain.CurrentDomain.BaseDirectory, path),
            Path.Combine(Directory.GetCurrentDirectory(), path),
            Path.Combine(Directory.GetCurrentDirectory(), "PenguinRunnerCS", path),
            Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "resources", "sprites", filename),
            Path.Combine(Directory.GetCurrentDirectory(), "resources", "sprites", filename),
            Path.Combine(Directory.GetCurrentDirectory(), "PenguinRunnerCS", "resources", "sprites", filename),
            Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "games", "default", "sprites", filename),
            Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "games", "ices", "sprites", filename),
            Path.Combine(Directory.GetCurrentDirectory(), "games", "default", "sprites", filename),
            Path.Combine(Directory.GetCurrentDirectory(), "games", "ices", "sprites", filename)
        };

        string? resolvedPath = null;
        foreach (string candidate in candidates)
        {
            if (File.Exists(candidate))
            {
                resolvedPath = candidate;
                break;
            }
        }

        if (resolvedPath == null)
        {
            if (filename.Equals("burnt.png", StringComparison.OrdinalIgnoreCase))
            {
                return LoadCachedTexture("burn.png");
            }
            if (filename.Equals("burn.png", StringComparison.OrdinalIgnoreCase))
            {
                return LoadCachedTexture("burnt.png");
            }
            return null;
        }

        if (textureCache.TryGetValue(resolvedPath, out var cachedTex))
        {
            return cachedTex;
        }

        try
        {
            var tex = Raylib.LoadTexture(resolvedPath);
            if (tex.Id > 0)
            {
                textureCache[resolvedPath] = tex;
                return tex;
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error loading texture from {resolvedPath}: {ex.Message}");
        }

        return null;
    }

    public void setSprite(string ruta, bool fromResource, bool modFolder)
    {
        string fullPath;
        if (fromResource)
        {
            fullPath = Path.Combine("resources", "sprites", ruta);
        }
        else
        {
            if (modFolder)
            {
                fullPath = ruta;
            }
            else
            {
                fullPath = Path.Combine(GamePanel.getFolderPath(), "sprites", ruta);
            }
        }

        pendingSpritePath = fullPath;
        if (Raylib.IsWindowReady())
        {
            sprite = LoadCachedTexture(fullPath);
        }
    }

    public void hardCodeSprite(Texture2D icon)
    {
        sprite = icon;
    }

    public virtual void setPrintables(string type)
    {
        var mapaSprites = GamePanel.getSpriteMap();
        List<string>? atributs = null;

        if (mapaSprites != null && mapaSprites.TryGetValue(type, out var found))
        {
            atributs = found;
        }
        else if (type.StartsWith("ice", StringComparison.OrdinalIgnoreCase))
        {
            string fn = type.EndsWith(".png", StringComparison.OrdinalIgnoreCase) ? type : type + ".png";
            atributs = new List<string> { "🧊", "#FFFFFF", fn };
        }
        else if (type.Equals("burnt", StringComparison.OrdinalIgnoreCase))
        {
            if (mapaSprites != null && mapaSprites.TryGetValue("burnt", out var bfound))
            {
                atributs = bfound;
            }
            else
            {
                atributs = new List<string> { "🔥", "#BB0000", "burnt.png" };
            }
        }
        else if (type.Equals("molten", StringComparison.OrdinalIgnoreCase))
        {
            if (mapaSprites != null && mapaSprites.TryGetValue("molten", out var mfound))
            {
                atributs = mfound;
            }
            else
            {
                atributs = new List<string> { "🕳️", "#23F0Fa", "molten.png" };
            }
        }

        if (atributs != null)
        {
            if (atributs.Count > 0)
            {
                setEmoji(atributs[0]);
            }
            if (atributs.Count > 1)
            {
                setColorFromHex(atributs[1]);
            }
            if (atributs.Count > 2)
            {
                if (atributs.Count > 3)
                {
                    setSprite(atributs[2], false, true);
                }
                else
                {
                    setSprite(atributs[2], !GamePanel.hasGame(), false);
                }
            }
        }
        else
        {
            setEmoji("#");
        }
    }

    public Color getColorPrintable() => color;
    public Color GetColorPrintable() => color;

    public void setColor(int r, int g, int b)
    {
        color = new Color(r, g, b, 255);
    }

    public void SetColor(int r, int g, int b) => setColor(r, g, b);

    public void setColorFromHex(string hex)
    {
        if (string.IsNullOrWhiteSpace(hex))
        {
            color = Color.White;
            return;
        }

        hex = hex.Trim().TrimStart('#');
        try
        {
            if (hex.Length == 6)
            {
                int r = Convert.ToInt32(hex.Substring(0, 2), 16);
                int g = Convert.ToInt32(hex.Substring(2, 2), 16);
                int b = Convert.ToInt32(hex.Substring(4, 2), 16);
                color = new Color(r, g, b, 255);
            }
            else if (hex.Length == 8)
            {
                int r = Convert.ToInt32(hex.Substring(0, 2), 16);
                int g = Convert.ToInt32(hex.Substring(2, 2), 16);
                int b = Convert.ToInt32(hex.Substring(4, 2), 16);
                int a = Convert.ToInt32(hex.Substring(6, 2), 16);
                color = new Color(r, g, b, a);
            }
            else
            {
                color = Color.White;
            }
        }
        catch
        {
            color = Color.White;
        }
    }

    public void SetColorFromHex(string hex) => setColorFromHex(hex);

    public Font getFont() => font;
    public static Font GetFont() => font;

    public static void setFont(Font f)
    {
        font = f;
        fontLoaded = true;
    }

    public static void SetFont(Font f) => setFont(f);

    public virtual void draw(int row, int col)
    {
        if ((!sprite.HasValue || sprite.Value.Id <= 0) && pendingSpritePath != null && Raylib.IsWindowReady())
        {
            sprite = LoadCachedTexture(pendingSpritePath);
            if ((!sprite.HasValue || sprite.Value.Id <= 0) && pendingSpritePath.Contains("ice"))
            {
                sprite = LoadCachedTexture("ice.png");
            }
        }

        if (sprite.HasValue && sprite.Value.Id > 0)
        {
            drawImage(row, col);
        }
        else
        {
            drawEmoji(row, col);
        }
    }

    public void Draw(int row, int col) => draw(row, col);

    public void drawImage(int row, int col)
    {
        if (!sprite.HasValue || sprite.Value.Id <= 0) return;

        float x = col * TILE_SIZE;
        float y = row * TILE_SIZE;

        Rectangle sourceRec = new Rectangle(0, 0, sprite.Value.Width, sprite.Value.Height);
        Rectangle destRec = new Rectangle(x, y, TILE_SIZE, TILE_SIZE);

        Raylib.DrawTexturePro(sprite.Value, sourceRec, destRec, Vector2.Zero, 0f, Color.White);
    }

    public void drawEmoji(int row, int col)
    {
        float cellX = col * TILE_SIZE;
        float cellY = row * TILE_SIZE;

        Font currentFont = fontLoaded ? font : Raylib.GetFontDefault();
        float fontSize = fontLoaded ? (font.BaseSize > 0 ? font.BaseSize : 24f) : 20f;
        float spacing = 1f;

        Vector2 size = Raylib.MeasureTextEx(currentFont, emoji, fontSize, spacing);
        float x = cellX + (TILE_SIZE - size.X) / 2f;
        float y = cellY + (TILE_SIZE - size.Y) / 2f;

        Raylib.DrawTextEx(currentFont, emoji, new Vector2(x, y), fontSize, spacing, color);
    }
}

