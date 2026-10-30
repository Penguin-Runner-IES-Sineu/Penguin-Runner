package gemini.penguinrunner.world;

/**
 * Ultra-fast bitmask autotiler with a 256-entry precomputed lookup table.
 * Zero string allocations during runtime tile updates!
 */
public final class IceAutoTiler {

    private static final int N  = 1;
    private static final int E  = 2;
    private static final int S  = 4;
    private static final int W  = 8;
    private static final int NE = 16;
    private static final int SE = 32;
    private static final int SW = 64;
    private static final int NW = 128;

    private static final String[] MASK_LUT = new String[256];

    static {
        for (int mask = 0; mask < 256; mask++) {
            MASK_LUT[mask] = computeLogicalKey(mask);
        }
    }

    private IceAutoTiler() {}

    public static String getAutotileKey(Block[][] blocks, int row, int col) {
        int mask = computeMask(blocks, row, col);
        return MASK_LUT[mask];
    }

    public static void updateAll(Block[][] blocks) {
        if (blocks == null || blocks.length == 0) return;
        for (int r = 0; r < blocks.length; r++) {
            if (blocks[r] == null) continue;
            for (int c = 0; c < blocks[r].length; c++) {
                Block b = blocks[r][c];
                if (b != null && b.type() == TileType.ICE) {
                    b.setAutotileKey(getAutotileKey(blocks, r, c));
                }
            }
        }
    }

    public static void updateDirtyNeighbors(Block[][] blocks, int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int r = row + dr;
                int c = col + dc;
                if (r >= 0 && r < blocks.length && c >= 0 && c < blocks[r].length) {
                    Block b = blocks[r][c];
                    if (b != null && b.type() == TileType.ICE) {
                        b.setAutotileKey(getAutotileKey(blocks, r, c));
                    }
                }
            }
        }
    }

    private static int computeMask(Block[][] blocks, int row, int col) {
        boolean n  = hasIce(blocks, row - 1, col);
        boolean e  = hasIce(blocks, row, col + 1);
        boolean s  = hasIce(blocks, row + 1, col);
        boolean w  = hasIce(blocks, row, col - 1);

        boolean ne = hasIce(blocks, row - 1, col + 1);
        boolean se = hasIce(blocks, row + 1, col + 1);
        boolean sw = hasIce(blocks, row + 1, col - 1);
        boolean nw = hasIce(blocks, row - 1, col - 1);

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

    private static boolean hasIce(Block[][] blocks, int row, int col) {
        if (row < 0 || row >= blocks.length) return false;
        if (blocks[row] == null || col < 0 || col >= blocks[row].length) return false;
        Block b = blocks[row][col];
        return b != null && b.type() == TileType.ICE;
    }

    private static String computeLogicalKey(int mask) {
        boolean n  = (mask & N) != 0;
        boolean e  = (mask & E) != 0;
        boolean s  = (mask & S) != 0;
        boolean w  = (mask & W) != 0;
        boolean ne = (mask & NE) != 0;
        boolean se = (mask & SE) != 0;
        boolean sw = (mask & SW) != 0;
        boolean nw = (mask & NW) != 0;

        int cardinal = 0;
        if (n) cardinal |= N;
        if (e) cardinal |= E;
        if (s) cardinal |= S;
        if (w) cardinal |= W;

        String key = switch (cardinal) {
            case 0 -> "ice_isolated";
            case N -> "ice_n";
            case E -> "ice_e";
            case S -> "ice_s";
            case W -> "ice_w";
            case N | S -> "ice_ns";
            case E | W -> "ice_ew";
            case N | E -> "ice_ne";
            case E | S -> "ice_es";
            case S | W -> "ice_sw";
            case W | N -> "ice_wn";
            case N | E | S -> "ice_nes";
            case E | S | W -> "ice_esw";
            case S | W | N -> "ice_swn";
            case W | N | E -> "ice_wne";
            case N | E | S | W -> "ice_center";
            default -> "ice_center";
        };

        StringBuilder sb = new StringBuilder(key);
        switch (cardinal) {
            case N | E -> { if (!ne) sb.append("_in_ne"); }
            case E | S -> { if (!se) sb.append("_in_se"); }
            case S | W -> { if (!sw) sb.append("_in_sw"); }
            case W | N -> { if (!nw) sb.append("_in_nw"); }
            case N | E | S -> {
                if (!ne) sb.append("_in_ne");
                if (!se) sb.append("_in_se");
            }
            case E | S | W -> {
                if (!se) sb.append("_in_se");
                if (!sw) sb.append("_in_sw");
            }
            case S | W | N -> {
                if (!sw) sb.append("_in_sw");
                if (!nw) sb.append("_in_nw");
            }
            case W | N | E -> {
                if (!nw) sb.append("_in_nw");
                if (!ne) sb.append("_in_ne");
            }
            case N | E | S | W -> {
                if (!ne) sb.append("_in_ne");
                if (!se) sb.append("_in_se");
                if (!sw) sb.append("_in_sw");
                if (!nw) sb.append("_in_nw");
            }
        }
        return sb.toString().intern();
    }
}

