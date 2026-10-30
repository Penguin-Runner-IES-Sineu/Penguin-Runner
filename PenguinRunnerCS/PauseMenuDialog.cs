using System.Numerics;
using Raylib_cs;

namespace iessineu.penguinrunner;

public class PauseMenuDialog
{
    public const int RESUME = 0;
    public const int SAVE = 1;
    public const int LOAD = 2;
    public const int EXIT = 3;

    private int selectedOption = RESUME;
    private float volume = 0.75f;
    private bool openDialog = false;

    private const int W = 688;
    private const int H = 516;

    private int getPlacaW() => Math.Min(380, Raylib.GetScreenWidth() - 40);
    private int getPlacaX() => (Raylib.GetScreenWidth() - getPlacaW()) / 2;

    private int getItemSpacing()
    {
        int available = Raylib.GetScreenHeight() - 110;
        return Math.Clamp(available / (labels.Length - 1), 36, 52);
    }

    private int getItemsStartY()
    {
        int spacing = getItemSpacing();
        int totalItemsH = (labels.Length - 1) * spacing;
        return (Raylib.GetScreenHeight() - totalItemsH) / 2 + 18;
    }

    private int getPlacaY() => getItemsStartY() - 52;
    private int getPlacaH() => (labels.Length - 1) * getItemSpacing() + 74;
    private int getButtonH() => Math.Min(36, getItemSpacing() - 6);

    private readonly string[] labels = {
        "Continuar",
        "Volum",
        "Guardar partida",
        "Carregar partida",
        "Sortir"
    };

    private readonly Color[] colors = {
        new Color(80, 220, 255, 255),
        new Color(170, 230, 255, 255),
        new Color(160, 235, 180, 255),
        new Color(255, 215, 120, 255),
        new Color(255, 130, 130, 255)
    };

    private int seleccionat = 0;
    private float cicle = 0f;
    private bool arrossegantVolum = false;

    private readonly GameState gameState;

    public PauseMenuDialog(GameState gameState)
    {
        this.gameState = gameState;
        this.volume = gameState.getGameVolume();
    }

    public void open(float currentVolume)
    {
        this.volume = currentVolume;
        this.openDialog = true;
        this.seleccionat = 0;
        this.arrossegantVolum = false;
    }

    public bool isOpen() => openDialog;
    public int getSelectedOption() => selectedOption;
    public float getVolume() => volume;

    public void update(float dt)
    {
        if (!openDialog) return;

        cicle += 0.03f;
        Vector2 mousePos = Raylib.GetMousePosition();

        int[] ys = posicionsY();
        for (int i = 0; i < ys.Length; i++)
        {
            if (mouseSobreBoto(mousePos, ys[i]))
            {
                seleccionat = i;
                if (Raylib.IsMouseButtonPressed(MouseButton.Left))
                {
                    activar();
                    return;
                }
                break;
            }
        }

        if (mouseSobreBarraVolum(mousePos))
        {
            if (Raylib.IsMouseButtonPressed(MouseButton.Left))
            {
                seleccionat = 1;
                arrossegantVolum = true;
            }
        }

        if (arrossegantVolum)
        {
            if (Raylib.IsMouseButtonDown(MouseButton.Left))
            {
                actualitzarVolumAmbRatoli(mousePos.X);
            }
            else
            {
                arrossegantVolum = false;
            }
        }

        // Teclat
        if (Raylib.IsKeyPressed(KeyboardKey.Up) || Raylib.IsKeyPressed(KeyboardKey.W))
        {
            seleccionat--;
            if (seleccionat < 0) seleccionat = labels.Length - 1;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Down) || Raylib.IsKeyPressed(KeyboardKey.S))
        {
            seleccionat++;
            if (seleccionat >= labels.Length) seleccionat = 0;
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Left) || Raylib.IsKeyPressed(KeyboardKey.A))
        {
            if (seleccionat == 1)
            {
                volume = Math.Clamp(volume - 0.05f, 0f, 1f);
                gameState.changeVolume(volume);
            }
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Right) || Raylib.IsKeyPressed(KeyboardKey.D))
        {
            if (seleccionat == 1)
            {
                volume = Math.Clamp(volume + 0.05f, 0f, 1f);
                gameState.changeVolume(volume);
            }
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Enter) || Raylib.IsKeyPressed(KeyboardKey.Space))
        {
            activar();
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Escape))
        {
            selectedOption = RESUME;
            openDialog = false;
        }
    }

    private void activar()
    {
        switch (seleccionat)
        {
            case 0:
                selectedOption = RESUME;
                openDialog = false;
                break;
            case 1:
                // Volume slider
                break;
            case 2:
                selectedOption = SAVE;
                openDialog = false;
                break;
            case 3:
                selectedOption = LOAD;
                openDialog = false;
                break;
            case 4:
                selectedOption = EXIT;
                openDialog = false;
                break;
        }
    }

    private int[] posicionsY()
    {
        int startY = getItemsStartY();
        int spacing = getItemSpacing();
        int[] ys = new int[labels.Length];
        for (int i = 0; i < labels.Length; i++)
        {
            ys[i] = startY + i * spacing;
        }
        return ys;
    }

    private bool mouseSobreBoto(Vector2 m, int y)
    {
        int px = getPlacaX();
        int pw = getPlacaW();
        int buttonX = px + 28;
        int h = getButtonH();
        int buttonY = y - h / 2;
        int buttonW = pw - 56;

        return m.X >= buttonX && m.X <= buttonX + buttonW &&
               m.Y >= buttonY && m.Y <= buttonY + h;
    }

    private bool mouseSobreBarraVolum(Vector2 m)
    {
        int px = getPlacaX();
        int pw = getPlacaW();
        int y = posicionsY()[1];
        int barX = px + 80;
        int barY = y + 4;
        int barW = pw - 160;
        int barH = 10;

        return m.X >= barX && m.X <= barX + barW &&
               m.Y >= barY - 8 && m.Y <= barY + barH + 8;
    }

    private void actualitzarVolumAmbRatoli(float mouseX)
    {
        int px = getPlacaX();
        int pw = getPlacaW();
        int barX = px + 80;
        int barW = pw - 160;

        float nouVolum = (mouseX - barX) / barW;
        volume = Math.Clamp(nouVolum, 0f, 1f);
        gameState.changeVolume(volume);
    }

    public void draw()
    {
        int screenW = Raylib.GetScreenWidth();
        int screenH = Raylib.GetScreenHeight();

        // Overlay fosc
        Raylib.DrawRectangle(0, 0, screenW, screenH, new Color(0, 0, 0, 180));

        // Placa
        int px = getPlacaX();
        int pw = getPlacaW();
        int placaY = getPlacaY();
        int placaH = getPlacaH();

        Raylib.DrawRectangleRounded(new Rectangle(px, placaY, pw, placaH), 0.1f, 8, new Color(5, 28, 70, 230));
        Raylib.DrawRectangleRoundedLines(new Rectangle(px, placaY, pw, placaH), 0.1f, 8, new Color(95, 210, 255, 200));

        // Títol
        string titol = "Pausa";
        int tw = Raylib.MeasureText(titol, 28);
        Raylib.DrawText(titol, px + (pw - tw) / 2, placaY + 12, 28, new Color(90, 220, 255, 255));

        // Botons
        int[] ys = posicionsY();
        for (int i = 0; i < labels.Length; i++)
        {
            if (i == 1)
            {
                dibuixarControlVolum(i, labels[i], colors[i], ys[i]);
            }
            else
            {
                dibuixarBoto(i, labels[i], colors[i], ys[i]);
            }
        }
    }

    private void dibuixarBoto(int idx, string label, Color color, int y)
    {
        bool sel = seleccionat == idx;
        int px = getPlacaX();
        int pw = getPlacaW();
        int x = px + 28;
        int w = pw - 56;
        int h = getButtonH();
        int buttonY = y - h / 2;

        if (sel)
        {
            Raylib.DrawRectangleRounded(new Rectangle(x, buttonY, w, h), 0.3f, 6, new Color((int)color.R, (int)color.G, (int)color.B, 120));
            Raylib.DrawRectangleRoundedLines(new Rectangle(x, buttonY, w, h), 0.3f, 6, color);
            dibuixarFletxaSelector(x - 18, y, color);
        }
        else
        {
            Raylib.DrawRectangleRounded(new Rectangle(x, buttonY, w, h), 0.3f, 6, new Color(0, 35, 90, 100));
        }

        int fontSize = sel ? 18 : 16;
        int tw = Raylib.MeasureText(label, fontSize);
        int tx = px + (pw - tw) / 2;

        Raylib.DrawText(label, tx, y - fontSize / 2, fontSize, sel ? Color.White : new Color(220, 235, 245, 220));
    }

    private void dibuixarControlVolum(int idx, string label, Color color, int y)
    {
        bool sel = seleccionat == idx;
        int px = getPlacaX();
        int pw = getPlacaW();
        int x = px + 28;
        int w = pw - 56;
        int h = getButtonH() + 6;
        int buttonY = y - h / 2;

        if (sel)
        {
            Raylib.DrawRectangleRounded(new Rectangle(x, buttonY, w, h), 0.3f, 6, new Color((int)color.R, (int)color.G, (int)color.B, 80));
            Raylib.DrawRectangleRoundedLines(new Rectangle(x, buttonY, w, h), 0.3f, 6, color);
            dibuixarFletxaSelector(x - 18, y, color);
        }
        else
        {
            Raylib.DrawRectangleRounded(new Rectangle(x, buttonY, w, h), 0.3f, 6, new Color(0, 35, 90, 90));
        }

        string text = label + " " + Math.Round(volume * 100) + "%";
        int tw = Raylib.MeasureText(text, 14);
        int tx = px + (pw - tw) / 2;
        Raylib.DrawText(text, tx, y - 14, 14, sel ? Color.White : new Color(220, 235, 245, 220));

        int barX = px + 80;
        int barY = y + 4;
        int barW = pw - 160;
        int barH = 10;

        Raylib.DrawRectangleRounded(new Rectangle(barX, barY, barW, barH), 0.5f, 4, new Color(0, 0, 0, 120));
        Raylib.DrawRectangleRounded(new Rectangle(barX, barY, barW * volume, barH), 0.5f, 4, new Color(80, 220, 255, 255));

        float knobX = barX + barW * volume;
        Raylib.DrawCircle((int)knobX, barY + barH / 2, 6, Color.White);
    }

    private void dibuixarFletxaSelector(int x, int y, Color color)
    {
        int offset = (int)(Math.Sin(cicle * 4) * 4);
        Vector2 v1 = new Vector2(x + offset, y - 8);
        Vector2 v2 = new Vector2(x + offset + 10, y);
        Vector2 v3 = new Vector2(x + offset, y + 8);

        Raylib.DrawTriangle(v1, v2, v3, color);
    }
}
