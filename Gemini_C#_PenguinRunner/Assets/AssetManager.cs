using System.Globalization;
using System.Text.Json;
using Raylib_cs;

namespace Gemini.PenguinRunner.Assets;

public class AssetManager : IDisposable
{
    private readonly string _resourceDir;
    private readonly Dictionary<string, SpriteConfig> _spriteConfigs = new(StringComparer.OrdinalIgnoreCase);
    private readonly Dictionary<string, Texture2D> _rawTextures = new(StringComparer.OrdinalIgnoreCase);
    private readonly Dictionary<string, Sound> _sounds = new(StringComparer.OrdinalIgnoreCase);
    private Music? _music;
    private float _volume = 0.85f;
    private bool _muted;
    private Font _uiFont;

    public float Volume => _volume;
    public bool IsMuted => _muted;
    public Font UIFont => _uiFont;

    public AssetManager(string resourceDir)
    {
        _resourceDir = resourceDir;
    }

    public void Initialize()
    {
        LoadFonts();
        LoadAudio();
        LoadPrintables();
    }

    public SpriteConfig? GetSprite(string key) => _spriteConfigs.GetValueOrDefault(key);
    public Texture2D? GetRawTexture(string key) => _rawTextures.TryGetValue(key, out var tex) ? tex : null;

    private void LoadFonts()
    {
        string fontPath = Path.Combine(_resourceDir, "font.ttf");
        if (File.Exists(fontPath))
        {
            _uiFont = Raylib.LoadFont(fontPath);
        }
        else
        {
            _uiFont = Raylib.GetFontDefault();
        }
    }

    private void LoadAudio()
    {
        Raylib.InitAudioDevice();

        string[] sfx = [
            "button", "collect", "death", "door", "flame", "gameover",
            "pickup", "popice", "teleport"
        ];

        foreach (var name in sfx)
        {
            string path = Path.Combine(_resourceDir, name + ".wav");
            if (File.Exists(path))
            {
                var snd = Raylib.LoadSound(path);
                Raylib.SetSoundVolume(snd, _volume);
                _sounds[name] = snd;
            }
        }

        string musicPath = Path.Combine(_resourceDir, "music.wav");
        if (File.Exists(musicPath))
        {
            _music = Raylib.LoadMusicStream(musicPath);
            Raylib.SetMusicVolume(_music.Value, _volume);
            Raylib.PlayMusicStream(_music.Value);
        }
    }

    private void LoadPrintables()
    {
        string jsonPath = Path.Combine(_resourceDir, "printables.json");
        if (!File.Exists(jsonPath)) return;

        try
        {
            string jsonText = File.ReadAllText(jsonPath);
            using var doc = JsonDocument.Parse(jsonText);
            foreach (var prop in doc.RootElement.EnumerateObject())
            {
                string key = prop.Name;
                if (prop.Value.ValueKind != JsonValueKind.Array) continue;
                var arr = prop.Value;
                if (arr.GetArrayLength() < 3) continue;

                string emoji = arr[0].GetString() ?? "";
                string hex = arr[1].GetString() ?? "#FFFFFF";
                string spriteRelPath = arr[2].GetString() ?? "";

                Color color = ParseHexColor(hex);

                Texture2D? tex = null;
                string spriteFile = Path.Combine(_resourceDir, "sprites", spriteRelPath);
                if (File.Exists(spriteFile))
                {
                    tex = Raylib.LoadTexture(spriteFile);
                    _rawTextures[key] = tex.Value;
                }

                _spriteConfigs[key] = new SpriteConfig(emoji, color, tex);
            }

            string selPath = Path.Combine(_resourceDir, "sprites", "selected.png");
            if (File.Exists(selPath))
            {
                _rawTextures["selected"] = Raylib.LoadTexture(selPath);
            }
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine($"Error loading printables.json: {ex.Message}");
        }
    }

    public void PlaySound(string name)
    {
        if (_muted) return;
        if (_sounds.TryGetValue(name, out var snd))
        {
            Raylib.PlaySound(snd);
        }
    }

    public void UpdateMusic()
    {
        if (_music.HasValue)
        {
            Raylib.UpdateMusicStream(_music.Value);
        }
    }

    public void SetVolume(float vol)
    {
        _volume = Math.Clamp(vol, 0f, 1f);
        if (_music.HasValue)
        {
            Raylib.SetMusicVolume(_music.Value, _muted ? 0f : _volume);
        }
        foreach (var snd in _sounds.Values)
        {
            Raylib.SetSoundVolume(snd, _volume);
        }
    }

    public void SetMuted(bool mute)
    {
        _muted = mute;
        if (_music.HasValue)
        {
            Raylib.SetMusicVolume(_music.Value, _muted ? 0f : _volume);
        }
    }

    private static Color ParseHexColor(string hex)
    {
        if (hex.StartsWith('#')) hex = hex[1..];
        if (uint.TryParse(hex, NumberStyles.HexNumber, CultureInfo.InvariantCulture, out uint val))
        {
            byte r = (byte)((val >> 16) & 0xFF);
            byte g = (byte)((val >> 8) & 0xFF);
            byte b = (byte)(val & 0xFF);
            return new Color((int)r, (int)g, (int)b, 255);
        }
        return Color.White;
    }

    public void Dispose()
    {
        if (_music.HasValue)
        {
            Raylib.UnloadMusicStream(_music.Value);
        }
        foreach (var snd in _sounds.Values)
        {
            Raylib.UnloadSound(snd);
        }
        foreach (var tex in _rawTextures.Values)
        {
            Raylib.UnloadTexture(tex);
        }
        Raylib.CloseAudioDevice();
    }
}
