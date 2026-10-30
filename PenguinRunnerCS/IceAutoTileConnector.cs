using iessineu.penguinrunner.Blocks;

namespace iessineu.penguinrunner;

public class IceAutoTileConnector
{
    private const int N = 1;
    private const int E = 2;
    private const int S = 4;
    private const int W = 8;

    private const int NE = 16;
    private const int SE = 32;
    private const int SW = 64;
    private const int NW = 128;

    public static void update(Block[][] blocks)
    {
        if (blocks == null || blocks.Length == 0)
        {
            return;
        }

        for (int row = 0; row < blocks.Length; row++)
        {
            if (blocks[row] == null)
            {
                continue;
            }

            for (int col = 0; col < blocks[row].Length; col++)
            {
                Block block = blocks[row][col];

                if (!isIce(block))
                {
                    continue;
                }

                string printableKey = getIcePrintableKey(blocks, row, col);
                block.setPrintables(printableKey);
            }
        }
    }

    public static void Update(Block[][] blocks) => update(blocks);

    private static string getIcePrintableKey(Block[][] blocks, int row, int col)
    {
        int mask = getMask(blocks, row, col);
        string logicalKey = getLogicalIceKey(mask);

        return getRealIceAssetName(logicalKey);
    }

    private static int getMask(Block[][] blocks, int row, int col)
    {
        bool n = hasIce(blocks, row - 1, col);
        bool e = hasIce(blocks, row, col + 1);
        bool s = hasIce(blocks, row + 1, col);
        bool w = hasIce(blocks, row, col - 1);

        bool ne = hasIce(blocks, row - 1, col + 1);
        bool se = hasIce(blocks, row + 1, col + 1);
        bool sw = hasIce(blocks, row + 1, col - 1);
        bool nw = hasIce(blocks, row - 1, col - 1);

        int mask = 0;

        if (n) mask |= N;
        if (e) mask |= E;
        if (s) mask |= S;
        if (w) mask |= W;

        /*
         * Les diagonals només compten si també existeixen
         * els dos costats que formen aquella cantonada.
         */
        if (n && e && ne) mask |= NE;
        if (e && s && se) mask |= SE;
        if (s && w && sw) mask |= SW;
        if (w && n && nw) mask |= NW;

        return mask;
    }

    private static string getLogicalIceKey(int mask)
    {
        bool n = (mask & N) != 0;
        bool e = (mask & E) != 0;
        bool s = (mask & S) != 0;
        bool w = (mask & W) != 0;

        bool ne = (mask & NE) != 0;
        bool se = (mask & SE) != 0;
        bool sw = (mask & SW) != 0;
        bool nw = (mask & NW) != 0;

        int cardinalMask = 0;

        if (n) cardinalMask |= N;
        if (e) cardinalMask |= E;
        if (s) cardinalMask |= S;
        if (w) cardinalMask |= W;

        string key = getBaseIceKey(cardinalMask);

        switch (cardinalMask)
        {
            case N | E:
                if (!ne) key += "_in_ne";
                break;

            case E | S:
                if (!se) key += "_in_se";
                break;

            case S | W:
                if (!sw) key += "_in_sw";
                break;

            case W | N:
                if (!nw) key += "_in_nw";
                break;

            case N | E | S:
                if (!ne) key += "_in_ne";
                if (!se) key += "_in_se";
                break;

            case E | S | W:
                if (!se) key += "_in_se";
                if (!sw) key += "_in_sw";
                break;

            case S | W | N:
                if (!sw) key += "_in_sw";
                if (!nw) key += "_in_nw";
                break;

            case W | N | E:
                if (!nw) key += "_in_nw";
                if (!ne) key += "_in_ne";
                break;

            case N | E | S | W:
                if (!ne) key += "_in_ne";
                if (!se) key += "_in_se";
                if (!sw) key += "_in_sw";
                if (!nw) key += "_in_nw";
                break;
        }

        return key;
    }

    private static string getBaseIceKey(int cardinalMask)
    {
        return cardinalMask switch
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

            N | E | S | W => "ice_center",

            _ => "ice_center"
        };
    }

    private static string getRealIceAssetName(string logicalKey)
    {
        return logicalKey;
    }

    private static bool hasIce(Block[][] blocks, int row, int col)
    {
        if (row < 0 || row >= blocks.Length)
        {
            return false;
        }

        if (blocks[row] == null)
        {
            return false;
        }

        if (col < 0 || col >= blocks[row].Length)
        {
            return false;
        }

        return isIce(blocks[row][col]);
    }

    private static bool isIce(Block? block)
    {
        return block != null && block.getType() == TileType.ICE;
    }
}

