using System.Numerics;
using Gemini.PenguinRunner.Assets;
using Raylib_cs;

namespace Gemini.PenguinRunner.UI;

public class PauseMenuDialog
{
    public const int Resume = 0;
    public const int Volume = 1;
    public const int Save   = 2;
    public const int Load   = 3;
    public const int Quit   = 4;

    private int _selectedIndex = 0;
    private bool _draggingVolume = false;

    private readonly string[] _options = ["Continue", "Volume", "Save Game", "Load Game", "Quit"];
    private readonly Color[] _colors = [
        new Color(80, 220, 255, 255),
        new Color(170, 230, 255, 255),
        new Color(160, 235, 180, 255),
        new Color(255, 215, 120, 255),
        new Color(255, 130, 130, 255)
    ];

    public bool IsOpen { get; set; }

    public void Open() => IsOpen = true;
    public void Close() => IsOpen = false;

    public void Update(AssetManager assets, Action onSave, Action onLoad)
    {
        if (!IsOpen) return;

        if (Raylib.IsKeyPressed(KeyboardKey.Escape))
        {
            Close();
            return;
        }

        if (Raylib.IsKeyPressed(KeyboardKey.Up) || Raylib.IsKeyPressed(KeyboardKey.W))
        {
            _selectedIndex = (_selectedIndex - 1 + _options.Length) % _options.Length;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Down) || Raylib.IsKeyPressed(KeyboardKey.S))
        {
            _selectedIndex = (_selectedIndex + 1) % _options.Length;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Left) || Raylib.IsKeyPressed(KeyboardKey.A))
        {
            if (_selectedIndex == Volume)
            {
                assets.SetVolume(assets.Volume - 0.05f);
            }
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Right) || Raylib.IsKeyPressed(KeyboardKey.D))
        {
            if (_selectedIndex == Volume)
            {
                assets.SetVolume(assets.Volume + 0.05f);
            }
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Enter) || Raylib.IsKeyPressed(KeyboardKey.Space))
        {
            ExecuteOption(_selectedIndex, onSave, onLoad);
        }

        // Mouse handling
        int screenW = Raylib.GetScreenWidth();
        int screenH = Raylib.GetScreenHeight();
        int placaW = Math.Min(360, screenW - 40);
        int placaH = Math.Min(320, screenH - 40);
        int placaX = (screenW - placaW) / 2;
        int placaY = (screenH - placaH) / 2;

        int startY = placaY + 60;
        int spacing = Math.Min(48, (placaH - 80) / _options.Length);

        Vector2 mouse = Raylib.GetMousePosition();

        if (Raylib.IsMouseButtonPressed(MouseButton.Left))
        {
            for (int i = 0; i < _options.Length; i++)
            {
                int itemY = startY + i * spacing;
                var rect = new Rectangle(placaX + 16, itemY - 14, placaW - 32, spacing - 4);
                if (Raylib.CheckCollisionPointRec(mouse, rect))
                {
                    _selectedIndex = i;
                    if (i == Volume)
                    {
                        _draggingVolume = true;
                        UpdateVolumeFromMouse(mouse.X, placaX + 120, placaW - 160, assets);
                    }
                    else
                    {
                        ExecuteOption(i, onSave, onLoad);
                    }
                    break;
                }
            }
        }

        if (Raylib.IsMouseButtonReleased(MouseButton.Left))
        {
            _draggingVolume = false;
        }

        if (_draggingVolume && Raylib.IsMouseButtonDown(MouseButton.Left))
        {
            UpdateVolumeFromMouse(mouse.X, placaX + 120, placaW - 160, assets);
        }
    }

    private void UpdateVolumeFromMouse(float mouseX, int sliderX, int sliderW, AssetManager assets)
    {
        float ratio = (mouseX - sliderX) / sliderW;
        assets.SetVolume(Math.Clamp(ratio, 0f, 1f));
    }

    private void ExecuteOption(int option, Action onSave, Action onLoad)
    {
        switch (option)
        {
            case Resume:
                Close();
                break;
            case Volume:
                break;
            case Save:
                onSave?.Invoke();
                Close();
                break;
            case Load:
                onLoad?.Invoke();
                Close();
                break;
            case Quit:
                Environment.Exit(0);
                break;
        }
    }

    public void Render(AssetManager assets)
    {
        if (!IsOpen) return;

        int screenW = Raylib.GetScreenWidth();
        int screenH = Raylib.GetScreenHeight();

        // Dim background
        Raylib.DrawRectangle(0, 0, screenW, screenH, new Color(0, 0, 0, 180));

        int placaW = Math.Min(360, screenW - 40);
        int placaH = Math.Min(320, screenH - 40);
        int placaX = (screenW - placaW) / 2;
        int placaY = (screenH - placaH) / 2;

        // Plaque background & border
        Raylib.DrawRectangleRounded(new Rectangle(placaX, placaY, placaW, placaH), 0.1f, 8, new Color(20, 26, 38, 240));
        Raylib.DrawRectangleRoundedLines(new Rectangle(placaX, placaY, placaW, placaH), 0.1f, 8, new Color(80, 140, 200, 255));

        // Title
        string title = "PAUSED";
        int titleW = Raylib.MeasureText(title, 24);
        Raylib.DrawText(title, (screenW - titleW) / 2, placaY + 20, 24, Color.White);

        int startY = placaY + 65;
        int spacing = Math.Min(48, (placaH - 80) / _options.Length);

        for (int i = 0; i < _options.Length; i++)
        {
            int itemY = startY + i * spacing;
            bool selected = (i == _selectedIndex);

            if (selected)
            {
                Raylib.DrawRectangleRounded(new Rectangle(placaX + 16, itemY - 6, placaW - 32, spacing - 6), 0.2f, 4, new Color(255, 255, 255, 30));
                Raylib.DrawText(_options[i], placaX + 28, itemY, 18, _colors[i]);
            }
            else
            {
                Raylib.DrawText(_options[i], placaX + 28, itemY, 18, new Color(200, 210, 220, 255));
            }

            if (i == Volume)
            {
                int sliderX = placaX + 120;
                int sliderY = itemY + 4;
                int sliderW = placaW - 160;
                int sliderH = 10;

                Raylib.DrawRectangleRounded(new Rectangle(sliderX, sliderY, sliderW, sliderH), 0.5f, 4, new Color(50, 60, 80, 255));
                int filledW = (int)(sliderW * assets.Volume);
                Raylib.DrawRectangleRounded(new Rectangle(sliderX, sliderY, filledW, sliderH), 0.5f, 4, new Color(80, 220, 255, 255));
                Raylib.DrawCircle(sliderX + filledW, sliderY + sliderH / 2, 8f, Color.White);
            }
        }
    }
}


