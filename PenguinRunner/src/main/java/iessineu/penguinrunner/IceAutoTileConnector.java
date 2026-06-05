/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

/**
 *
 * @author loren
 */
import java.io.Serializable;

import iessineu.penguinrunner.Blocks.Block;
import iessineu.penguinrunner.Blocks.TileType;

public class IceAutoTileConnector implements Serializable{

    private static final int N = 1;
    private static final int E = 2;
    private static final int S = 4;
    private static final int W = 8;

    private static final int NE = 16;
    private static final int SE = 32;
    private static final int SW = 64;
    private static final int NW = 128;

    public static void update(Block[][] blocks) {
        if (blocks == null || blocks.length == 0) {
            return;
        }

        for (int row = 0; row < blocks.length; row++) {
            if (blocks[row] == null) {
                continue;
            }

            for (int col = 0; col < blocks[row].length; col++) {
                Block block = blocks[row][col];

                if (!isIce(block)) {
                    continue;
                }

                String printableKey = getIcePrintableKey(blocks, row, col);
                // System.out.println(printableKey);

                block.setPrintables(printableKey);
                // block.setPrintables();
            }
        }
    }

    private static String getIcePrintableKey(Block[][] blocks, int row, int col) {
        int mask = getMask(blocks, row, col);
        String logicalKey = getLogicalIceKey(mask);

        return getRealIceAssetName(logicalKey);
    }

    private static int getMask(Block[][] blocks, int row, int col) {
        boolean n = hasIce(blocks, row - 1, col);
        boolean e = hasIce(blocks, row, col + 1);
        boolean s = hasIce(blocks, row + 1, col);
        boolean w = hasIce(blocks, row, col - 1);

        boolean ne = hasIce(blocks, row - 1, col + 1);
        boolean se = hasIce(blocks, row + 1, col + 1);
        boolean sw = hasIce(blocks, row + 1, col - 1);
        boolean nw = hasIce(blocks, row - 1, col - 1);

        int mask = 0;

        if (n) {
            mask |= N;
        }
        if (e) {
            mask |= E;
        }
        if (s) {
            mask |= S;
        }
        if (w) {
            mask |= W;
        }

        /*
         * Les diagonals només compten si també existeixen
         * els dos costats que formen aquella cantonada.
         *
         * Per exemple:
         * NE només importa si hi ha N i E.
         */
        if (n && e && ne) {
            mask |= NE;
        }
        if (e && s && se) {
            mask |= SE;
        }
        if (s && w && sw) {
            mask |= SW;
        }
        if (w && n && nw) {
            mask |= NW;
        }

        return mask;
    }

    private static String getLogicalIceKey(int mask) {
        boolean n = (mask & N) != 0;
        boolean e = (mask & E) != 0;
        boolean s = (mask & S) != 0;
        boolean w = (mask & W) != 0;

        boolean ne = (mask & NE) != 0;
        boolean se = (mask & SE) != 0;
        boolean sw = (mask & SW) != 0;
        boolean nw = (mask & NW) != 0;

        int cardinalMask = 0;

        if (n) {
            cardinalMask |= N;
        }
        if (e) {
            cardinalMask |= E;
        }
        if (s) {
            cardinalMask |= S;
        }
        if (w) {
            cardinalMask |= W;
        }

        String key = getBaseIceKey(cardinalMask);

        switch (cardinalMask) {
            case N | E -> {
                if (!ne) {
                    key += "_in_ne";
                }
            }

            case E | S -> {
                if (!se) {
                    key += "_in_se";
                }
            }

            case S | W -> {
                if (!sw) {
                    key += "_in_sw";
                }
            }

            case W | N -> {
                if (!nw) {
                    key += "_in_nw";
                }
            }

            case N | E | S -> {
                if (!ne) {
                    key += "_in_ne";
                }
                if (!se) {
                    key += "_in_se";
                }
            }

            case E | S | W -> {
                if (!se) {
                    key += "_in_se";
                }
                if (!sw) {
                    key += "_in_sw";
                }
            }

            case S | W | N -> {
                if (!sw) {
                    key += "_in_sw";
                }
                if (!nw) {
                    key += "_in_nw";
                }
            }

            case W | N | E -> {
                if (!nw) {
                    key += "_in_nw";
                }
                if (!ne) {
                    key += "_in_ne";
                }
            }

            case N | E | S | W -> {
                if (!ne) {
                    key += "_in_ne";
                }
                if (!se) {
                    key += "_in_se";
                }
                if (!sw) {
                    key += "_in_sw";
                }
                if (!nw) {
                    key += "_in_nw";
                }
            }
        }

        return key;
    }

    private static String getBaseIceKey(int cardinalMask) {
        return switch (cardinalMask) {
            case 0 ->
                "ice_isolated";

            case N ->
                "ice_n";
            case E ->
                "ice_e";
            case S ->
                "ice_s";
            case W ->
                "ice_w";

            case N | S ->
                "ice_ns";
            case E | W ->
                "ice_ew";

            case N | E ->
                "ice_ne";
            case E | S ->
                "ice_es";
            case S | W ->
                "ice_sw";
            case W | N ->
                "ice_wn";

            case N | E | S ->
                "ice_nes";
            case E | S | W ->
                "ice_esw";
            case S | W | N ->
                "ice_swn";
            case W | N | E ->
                "ice_wne";

            case N | E | S | W ->
                "ice_center";

            default ->
                "ice_center";
        };
    }

    private static String getRealIceAssetName(String logicalKey) {
        return logicalKey;
    }

    private static boolean hasIce(Block[][] blocks, int row, int col) {
        if (row < 0 || row >= blocks.length) {
            return false;
        }

        if (blocks[row] == null) {
            return false;
        }

        if (col < 0 || col >= blocks[row].length) {
            return false;
        }

        return isIce(blocks[row][col]);
    }

    private static boolean isIce(Block block) {
        return block != null && block.getType() == TileType.ICE;
    }
}
