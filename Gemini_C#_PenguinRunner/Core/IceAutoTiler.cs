using System.Text;

namespace Gemini.PenguinRunner.Core;

public static class IceAutoTiler
{
    private const int N  = 1;
    private const int E  = 2;
    private const int S  = 4;
    private const int W  = 8;
    private const int NE = 16;
    private const int SE = 32;
    private const int SW = 64;
    private const int NW = 128;

    private static readonly string[] MaskLut = new string[256];

    static IceAutoTiler()
    {
        for (int mask = 0; mask < 256; mask++)
        {
            MaskLut[mask] = ComputeLogicalKey(mask);
        }
    }

    public static string GetAutotileKey(Block?[,] blocks, int row, int col)
    {
        int mask = ComputeMask(blocks, row, col);
        return MaskLut[mask];
    }

    public static void UpdateAll(Block?[,] blocks)
    {
        int rows = blocks.GetLength(0);
        int cols = blocks.GetLength(1);

        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < cols; c++)
            {
                var b = blocks[r, c];
                if (b != null && b.Type == TileType.Ice)
                {
                    b.AutotileKey = GetAutotileKey(blocks, r, c);
                }
            }
        }
    }

    public static void UpdateDirtyNeighbors(Block?[,] blocks, int row, int col)
    {
        int rows = blocks.GetLength(0);
        int cols = blocks.GetLength(1);

        for (int dr = -1; dr <= 1; dr++)
        {
            for (int dc = -1; dc <= 1; dc++)
            {
                int r = row + dr;
                int c = col + dc;
                if (r >= 0 && r < rows && c >= 0 && c < cols)
                {
                    var b = blocks[r, c];
                    if (b != null && b.Type == TileType.Ice)
                    {
                        b.AutotileKey = GetAutotileKey(blocks, r, c);
                    }
                }
            }
        }
    }

    private static int ComputeMask(Block?[,] blocks, int row, int col)
    {
        bool n  = HasIce(blocks, row - 1, col);
        bool e  = HasIce(blocks, row, col + 1);
        bool s  = HasIce(blocks, row + 1, col);
        bool w  = HasIce(blocks, row, col - 1);

        bool ne = HasIce(blocks, row - 1, col + 1);
        bool se = HasIce(blocks, row + 1, col + 1);
        bool sw = HasIce(blocks, row + 1, col - 1);
        bool nw = HasIce(blocks, row - 1, col - 1);

        int mask = 0;
        if (n) mask |= N;
        if (e) mask |= E;
        if (s) mask |= S;
        if (w) mask |= W;

        if (n && e && ne) mask |= NE;
        if (e && s && se) mask |= SE;
        if (s && w && sw) mask |= SW;
        if (w && n && nw) mask |= NW;

        return mask;
    }

    private static bool HasIce(Block?[,] blocks, int row, int col)
    {
        int rows = blocks.GetLength(0);
        int cols = blocks.GetLength(1);
        if (row < 0 || row >= rows || col < 0 || col >= cols) return false;
        var b = blocks[row, col];
        return b != null && b.Type == TileType.Ice;
    }

    private static string ComputeLogicalKey(int mask)
    {
        bool n  = (mask & N) != 0;
        bool e  = (mask & E) != 0;
        bool s  = (mask & S) != 0;
        bool w  = (mask & W) != 0;
        bool ne = (mask & NE) != 0;
        bool se = (mask & SE) != 0;
        bool sw = (mask & SW) != 0;
        bool nw = (mask & NW) != 0;

        int cardinal = 0;
        if (n) cardinal |= N;
        if (e) cardinal |= E;
        if (s) cardinal |= S;
        if (w) cardinal |= W;

        string key = cardinal switch
        {
            0 => "ice_isolated",
            N => "ice_n",
            E => "ice_e",
            S => "ice_s",
            W => "ice_w",
            N | S => "ice_ns",
            E | W => "ice_ew",
            N | E => "ice_ne",
            E | S => "ice_es",
            S | W => "ice_sw",
            W | N => "ice_wn",
            N | E | S => "ice_nes",
            E | S | W => "ice_esw",
            S | W | N => "ice_swn",
            W | N | E => "ice_wne",
            _ => "ice_center"
        };

        var sb = new StringBuilder(key);
        switch (cardinal)
        {
            case N | E:
                if (!ne) sb.Append("_in_ne");
                break;
            case E | S:
                if (!se) sb.Append("_in_se");
                break;
            case S | W:
                if (!sw) sb.Append("_in_sw");
                break;
            case W | N:
                if (!nw) sb.Append("_in_nw");
                break;
            case N | E | S:
                if (!ne) sb.Append("_in_ne");
                if (!se) sb.Append("_in_se");
                break;
            case E | S | W:
                if (!se) sb.Append("_in_se");
                if (!sw) sb.Append("_in_sw");
                break;
            case S | W | N:
                if (!sw) sb.Append("_in_sw");
                if (!nw) sb.Append("_in_nw");
                break;
            case W | N | E:
                if (!nw) sb.Append("_in_nw");
                if (!ne) sb.Append("_in_ne");
                break;
            case N | E | S | W:
                if (!ne) sb.Append("_in_ne");
                if (!se) sb.Append("_in_se");
                if (!sw) sb.Append("_in_sw");
                if (!nw) sb.Append("_in_nw");
                break;
        }

        return string.Intern(sb.ToString());
    }
}
