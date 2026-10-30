using System.Numerics;
using Raylib_cs;

namespace iessineu.penguinrunner;

public class MenuPanel
{
    public const int W = 688;
    public const int H = 516;

    private bool arrancar = false;
    private int seleccionat = 0;
    private float cicle = 0f;
    private float alphaMenu = 0f;

    private const int PLACA_X = 148;
    private const int PLACA_W = 392;
    private const int ITEMS_START_Y = 250;
    private const int ITEM_SPACING = 66;

    private static readonly Color C_JUGAR = new Color(80, 220, 255, 255);
    private static readonly Color C_SORTIR = new Color(255, 130, 130, 255);
    private static readonly Color C_REINTENTAR = new Color(255, 180, 80, 255);
    private static readonly Color C_VOLUM = new Color(170, 230, 255, 255);

    private Color[] colors;
    private string[] labels;

    private bool deathMenu = false;
    private bool obrirOpcions = false;
    private bool menuOpcions = false;

    private float volum = 0.75f;
    private bool arrossegantVolum = false;

    private static readonly int[][] ESTRELLES = {
        new[] {60, 30}, new[] {140, 55}, new[] {220, 22}, new[] {310, 48}, new[] {400, 18}, new[] {500, 52}, new[] {590, 28}, new[] {650, 70},
        new[] {100, 95}, new[] {280, 82}, new[] {460, 92}, new[] {640, 105}, new[] {30, 115}, new[] {180, 130}, new[] {360, 118}, new[] {520, 135}
    };

    private static readonly int[][] NEU = {
        new[] {45, 190}, new[] {92, 235}, new[] {130, 175}, new[] {205, 220}, new[] {260, 185}, new[] {325, 238},
        new[] {380, 170}, new[] {435, 225}, new[] {510, 200}, new[] {560, 250}, new[] {620, 180}, new[] {665, 235}
    };

    private Texture2D? deathEnemyTexture = null;

    public MenuPanel(bool deathMenu)
    {
        this.deathMenu = deathMenu;

        if (deathMenu)
        {
            labels = new[] { "Tornar a intentar", "Opcions", "Sortir" };
            colors = new[] { C_REINTENTAR, C_VOLUM, C_SORTIR };

            string enemyPath = Path.Combine("resources", "sprites", "enemy.png");
            if (!File.Exists(enemyPath))
            {
                enemyPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, enemyPath);
            }
            if (File.Exists(enemyPath))
            {
                deathEnemyTexture = Printable.LoadCachedTexture(enemyPath);
            }
        }
        else
        {
            labels = new[] { "Comencar partida", "Opcions", "Sortir" };
            colors = new[] { C_JUGAR, C_VOLUM, C_SORTIR };
        }
    }

    public void update(float dt)
    {
        cicle += 0.03f;
        alphaMenu = Math.Min(1f, alphaMenu + 0.02f);

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

        if (menuOpcions && mouseSobreBarraVolum(mousePos))
        {
            if (Raylib.IsMouseButtonPressed(MouseButton.Left))
            {
                seleccionat = 0;
                arrossegantVolum = true;
            }
        }

        if (menuOpcions && arrossegantVolum)
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
            if (menuOpcions && seleccionat == 0)
            {
                volum = Math.Clamp(volum - 0.05f, 0f, 1f);
            }
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Right) || Raylib.IsKeyPressed(KeyboardKey.D))
        {
            if (menuOpcions && seleccionat == 0)
            {
                volum = Math.Clamp(volum + 0.05f, 0f, 1f);
            }
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Enter) || Raylib.IsKeyPressed(KeyboardKey.Space))
        {
            activar();
        }
        else if (Raylib.IsKeyPressed(KeyboardKey.Escape))
        {
            if (menuOpcions)
            {
                tornarDeMenuOpcions();
            }
            else
            {
                Environment.Exit(0);
            }
        }
    }

    public void Update(float dt) => update(dt);

    private void activar()
    {
        if (menuOpcions)
        {
            if (seleccionat == labels.Length - 1)
            {
                tornarDeMenuOpcions();
            }
            return;
        }

        if (seleccionat == 0)
        {
            arrancar = true;
            return;
        }

        if (seleccionat == 1)
        {
            convertirEnMenuOpcions();
            return;
        }

        if (seleccionat == labels.Length - 1)
        {
            Environment.Exit(0);
        }
    }

    private int[] posicionsY()
    {
        int[] ys = new int[labels.Length];
        for (int i = 0; i < labels.Length; i++)
        {
            ys[i] = ITEMS_START_Y + i * ITEM_SPACING;
        }
        return ys;
    }

    private bool mouseSobreBoto(Vector2 m, int y)
    {
        int buttonX = PLACA_X + 28;
        int buttonY = y - 24;
        int buttonW = PLACA_W - 56;
        int buttonH = 48;

        return m.X >= buttonX && m.X <= buttonX + buttonW &&
               m.Y >= buttonY && m.Y <= buttonY + buttonH;
    }

    private bool mouseSobreBarraVolum(Vector2 m)
    {
        int y = posicionsY()[0];
        int barX = PLACA_X + 92;
        int barY = y + 12;
        int barW = PLACA_W - 184;
        int barH = 18;

        return m.X >= barX && m.X <= barX + barW &&
               m.Y >= barY - 8 && m.Y <= barY + barH + 8;
    }

    private void actualitzarVolumAmbRatoli(float mouseX)
    {
        int barX = PLACA_X + 92;
        int barW = PLACA_W - 184;

        float nouVolum = (mouseX - barX) / barW;
        volum = Math.Clamp(nouVolum, 0f, 1f);
    }

    public void draw()
    {
        dibuixarFons();
        dibuixarLlumsFons();
        dibuixarEstrelles();
        dibuixarNeu();
        dibuixarOnes();

        dibuixarPinguiAnimat();

        string titol;
        string sub;

        if (menuOpcions)
        {
            titol = "Opcions";
            sub = "Ajusta el joc al teu gust.";
        }
        else if (deathMenu)
        {
            titol = "Has caigut!";
            sub = "Respira, torna-ho a provar i agafa tots els gelats.";
        }
        else
        {
            titol = "Penguin Runner";
            sub = "Gel, escales, enemics i molts gelats.";
        }

        dibuixarTitol(titol, sub);
        dibuixarPlaca();
        dibuixarItemsMenu();
    }

    public void Draw() => draw();

    private void dibuixarFons()
    {
        if (deathMenu)
        {
            Raylib.DrawRectangleGradientV(0, 0, W, (int)(H * 0.65f), new Color(30, 4, 8, 255), new Color(82, 10, 12, 255));
            Raylib.DrawRectangleGradientV(0, (int)(H * 0.65f), W, (int)(H * 0.35f), new Color(18, 18, 18, 255), new Color(42, 35, 35, 255));
        }
        else
        {
            Raylib.DrawRectangleGradientV(0, 0, W, (int)(H * 0.65f), new Color(4, 12, 45, 255), new Color(8, 43, 95, 255));
            Raylib.DrawRectangleGradientV(0, (int)(H * 0.65f), W, (int)(H * 0.35f), new Color(118, 188, 232, 255), new Color(215, 240, 255, 255));
        }
    }

    private void dibuixarLlumsFons()
    {
        Color light = deathMenu ? new Color(255, 120, 70, 45) : new Color(110, 220, 255, 45);
        Raylib.DrawCircleGradient(new Vector2((int)(W * 0.5f), (int)(H * 0.2f)), 250f, light, new Color(0, 0, 0, 0));
    }

    private void dibuixarEstrelles()
    {
        foreach (int[] est in ESTRELLES)
        {
            float brill = 0.5f + 0.5f * (float)Math.Sin(cicle * 1.4 + est[0] * 0.07);
            byte a = (byte)(brill * 255);
            Raylib.DrawCircle(est[0], est[1], 2, new Color((byte)255, (byte)255, (byte)255, a));
        }
    }

    private void dibuixarNeu()
    {
        for (int i = 0; i < NEU.Length; i++)
        {
            int baseX = NEU[i][0];
            int baseY = NEU[i][1];

            int x = (int)(baseX + Math.Sin(cicle + i) * 8);
            int y = (int)((baseY + cicle * 18 + i * 13) % H);

            float alpha = 0.3f + 0.3f * (float)Math.Sin(cicle + i * 0.8f);
            byte a = (byte)(alpha * 255);

            Raylib.DrawCircle(x, y, 2.5f, new Color((byte)255, (byte)255, (byte)255, a));
        }
    }

    private void dibuixarOnes()
    {
        for (int i = 0; i < 3; i++)
        {
            int oy = (int)(H * 0.625f) + i * 10;
            byte a = (byte)(70 + i * 20);
            Color col = deathMenu ? new Color((byte)20, (byte)10, (byte)10, a) : new Color((byte)15, (byte)65, (byte)145, a);

            for (int x = 0; x < W; x += 4)
            {
                double yy = oy + Math.Sin(x / 55.0 + cicle * 1.6 + i) * 5;
                Raylib.DrawRectangle(x, (int)yy, 5, H - (int)yy, col);
            }
        }
    }

    private void dibuixarPinguiAnimat()
    {
        float py = H * 0.3f + (float)Math.Sin(cicle) * 7f;
        float escala = 1f + 0.025f * (float)Math.Sin(cicle * 1.6f);

        int w = (int)(80 * escala);
        int h = (int)(110 * escala);
        int cx = (int)(W * 0.68f);

        // Ombra
        Raylib.DrawEllipse(cx, (int)py + 100, 42, 7, new Color(0, 0, 0, 50));

        if (deathMenu && deathEnemyTexture.HasValue && deathEnemyTexture.Value.Id > 0)
        {
            Raylib.DrawTexturePro(deathEnemyTexture.Value,
                new Rectangle(0, 0, deathEnemyTexture.Value.Width, deathEnemyTexture.Value.Height),
                new Rectangle(cx - w / 2, py, w, h),
                Vector2.Zero, 0f, Color.White);
            return;
        }

        // Cos
        Raylib.DrawEllipse(cx, (int)py + 66, 25, 34, new Color(18, 18, 35, 255));
        // Panxa blanca
        Raylib.DrawEllipse(cx, (int)py + 68, 18, 24, new Color(240, 248, 255, 255));
        // Cap
        Raylib.DrawEllipse(cx, (int)py + 30, 23, 22, new Color(18, 18, 35, 255));
        // Cara blanca
        Raylib.DrawEllipse(cx, (int)py + 31, 18, 17, new Color(240, 248, 255, 255));

        // Ulls
        Raylib.DrawCircle(cx - 7, (int)py + 26, 5, Color.Black);
        Raylib.DrawCircle(cx - 6, (int)py + 25, 2, Color.White);
        Raylib.DrawCircle(cx + 7, (int)py + 26, 5, Color.Black);
        Raylib.DrawCircle(cx + 8, (int)py + 25, 2, Color.White);

        // Bec taronja
        Raylib.DrawTriangle(
            new Vector2(cx - 7, py + 33),
            new Vector2(cx + 7, py + 33),
            new Vector2(cx, py + 41),
            new Color(255, 160, 0, 255)
        );

        // Aletes
        Raylib.DrawEllipse(cx - 24, (int)py + 60, 7, 19, new Color(18, 18, 35, 255));
        Raylib.DrawEllipse(cx + 24, (int)py + 60, 7, 19, new Color(18, 18, 35, 255));

        // Peus
        Raylib.DrawRectangleRounded(new Rectangle(cx - 18, py + 95, 16, 9), 0.5f, 4, new Color(255, 160, 0, 255));
        Raylib.DrawRectangleRounded(new Rectangle(cx + 2, py + 95, 16, 9), 0.5f, 4, new Color(255, 160, 0, 255));

        // Bufanda vermella
        Raylib.DrawRectangleRounded(new Rectangle(cx - 25, py + 46, 50, 9), 0.5f, 4, new Color(210, 40, 40, 255));
        Raylib.DrawRectangle(cx - 3, (int)py + 55, 6, 14, new Color(170, 25, 25, 255));
    }

    private void dibuixarTitol(string titol, string sub)
    {
        int tw = Raylib.MeasureText(titol, 44);
        int tx = (W - tw) / 2;

        Raylib.DrawText(titol, tx + 3, 75 + 3, 44, new Color(0, 40, 80, 150));
        Color col = deathMenu ? new Color(255, 170, 110, 255) : new Color(90, 220, 255, 255);
        Raylib.DrawText(titol, tx, 75, 44, col);

        int sw = Raylib.MeasureText(sub, 14);
        int sx = (W - sw) / 2;
        Raylib.DrawText(sub, sx, 130, 14, deathMenu ? new Color(255, 215, 190, 220) : new Color(175, 230, 255, 220));
    }

    private void dibuixarPlaca()
    {
        int[] ys = posicionsY();
        int placaH = ys[^1] - ITEMS_START_Y + 70;
        int placaY = ITEMS_START_Y - 38;

        Color bg = deathMenu ? new Color(35, 16, 18, 220) : new Color(5, 28, 70, 220);
        Color border = deathMenu ? new Color(255, 160, 100, 140) : new Color(95, 210, 255, 140);

        Raylib.DrawRectangleRounded(new Rectangle(PLACA_X, placaY, PLACA_W, placaH), 0.15f, 8, bg);
        Raylib.DrawRectangleRoundedLines(new Rectangle(PLACA_X, placaY, PLACA_W, placaH), 0.15f, 8, border);
    }

    private void dibuixarItemsMenu()
    {
        int[] ys = posicionsY();
        for (int i = 0; i < labels.Length; i++)
        {
            if (menuOpcions && i == 0)
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
        int x = PLACA_X + 28;
        int w = PLACA_W - 56;
        int h = 48;

        if (sel)
        {
            Raylib.DrawRectangleRounded(new Rectangle(x, y - 22, w, h), 0.35f, 6, new Color((int)color.R, (int)color.G, (int)color.B, 115));
            Raylib.DrawRectangleRoundedLines(new Rectangle(x, y - 22, w, h), 0.35f, 6, new Color((int)color.R, (int)color.G, (int)color.B, 220));
            dibuixarFletxaSelector(x - 22, y, color);
        }
        else
        {
            Color idleBg = deathMenu ? new Color(55, 25, 20, 110) : new Color(0, 35, 90, 95);
            Raylib.DrawRectangleRounded(new Rectangle(x, y - 20, w, h - 4), 0.35f, 6, idleBg);
        }

        int fontSize = sel ? 20 : 18;
        int tw = Raylib.MeasureText(label, fontSize);
        int tx = PLACA_X + (PLACA_W - tw) / 2;

        Raylib.DrawText(label, tx, y - 8, fontSize, sel ? Color.White : new Color(220, 235, 245, 220));
    }

    private void dibuixarControlVolum(int idx, string label, Color color, int y)
    {
        bool sel = seleccionat == idx;
        int x = PLACA_X + 28;
        int w = PLACA_W - 56;
        int h = 58;

        if (sel)
        {
            Raylib.DrawRectangleRounded(new Rectangle(x, y - 24, w, h), 0.35f, 6, new Color((int)color.R, (int)color.G, (int)color.B, 60));
            Raylib.DrawRectangleRoundedLines(new Rectangle(x, y - 24, w, h), 0.35f, 6, color);
            dibuixarFletxaSelector(x - 22, y, color);
        }
        else
        {
            Color idleBg = deathMenu ? new Color(55, 25, 20, 100) : new Color(0, 35, 90, 85);
            Raylib.DrawRectangleRounded(new Rectangle(x, y - 22, w, h - 4), 0.35f, 6, idleBg);
        }

        string text = label + " " + Math.Round(volum * 100) + "%";
        int tw = Raylib.MeasureText(text, 17);
        int tx = PLACA_X + (PLACA_W - tw) / 2;
        Raylib.DrawText(text, tx, y - 18, 17, sel ? Color.White : new Color(220, 235, 245, 220));

        int barX = PLACA_X + 92;
        int barY = y + 10;
        int barW = PLACA_W - 184;
        int barH = 14;

        Raylib.DrawRectangleRounded(new Rectangle(barX, barY, barW, barH), 0.5f, 4, new Color(0, 0, 0, 110));
        Raylib.DrawRectangleRounded(new Rectangle(barX, barY, barW * volum, barH), 0.5f, 4, new Color(80, 220, 255, 255));

        float knobX = barX + barW * volum;
        Raylib.DrawCircle((int)knobX, barY + barH / 2, 8, Color.White);
    }

    private void dibuixarFletxaSelector(int x, int y, Color color)
    {
        int offset = (int)(Math.Sin(cicle * 4) * 4);
        Vector2 v1 = new Vector2(x + offset, y - 9);
        Vector2 v2 = new Vector2(x + offset + 12, y);
        Vector2 v3 = new Vector2(x + offset, y + 9);

        Raylib.DrawTriangle(v1, v2, v3, color);
    }

    public void convertirEnMenuOpcions()
    {
        menuOpcions = true;
        seleccionat = 0;
        labels = new[] { "Volum", "Tornar" };
        colors = new[] { C_VOLUM, C_SORTIR };
    }

    public void tornarDeMenuOpcions()
    {
        menuOpcions = false;
        seleccionat = 0;
        if (deathMenu)
        {
            labels = new[] { "Tornar a intentar", "Opcions", "Sortir" };
            colors = new[] { C_REINTENTAR, C_VOLUM, C_SORTIR };
        }
        else
        {
            labels = new[] { "Comencar partida", "Opcions", "Sortir" };
            colors = new[] { C_JUGAR, C_VOLUM, C_SORTIR };
        }
    }

    public bool volObrirOpcions() => obrirOpcions;
    public void resetObrirOpcions() => obrirOpcions = false;
    public float getVolum() => volum;
    public bool arrancar_() => arrancar;
    public bool isOpen() => !arrancar;

    public void forceStart()
    {
        arrancar = true;
    }
}
