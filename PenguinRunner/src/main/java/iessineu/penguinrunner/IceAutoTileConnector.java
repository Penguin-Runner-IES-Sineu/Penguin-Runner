/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

/**
 *
 * @author loren
 */


import iessineu.penguinrunner.Blocks.Block;
import iessineu.penguinrunner.Blocks.TileType;

public class IceAutoTileConnector {

    public static void update(Block[][] blocks) {
        if (blocks == null || blocks.length == 0 || blocks[0].length == 0) {
            return;
        }

        for (int row = 0; row < blocks.length; row++) {
            for (int col = 0; col < blocks[0].length; col++) {
                Block block = blocks[row][col];

                if (!isIce(block)) {
                    continue;
                }

                String printableKey = getIcePrintableKey(blocks, row, col);

                block.setPrintableKey(printableKey);
                block.setPrintables();
            }
        }
    }

    private static String getIcePrintableKey(Block[][] blocks, int row, int col) {
        boolean n = hasIce(blocks, row - 1, col);
        boolean e = hasIce(blocks, row, col + 1);
        boolean s = hasIce(blocks, row + 1, col);
        boolean w = hasIce(blocks, row, col - 1);

        int mask = 0;

        if (n) {
            mask |= 1;
        }

        if (e) {
            mask |= 2;
        }

        if (s) {
            mask |= 4;
        }

        if (w) {
            mask |= 8;
        }

        if (mask == 15) {
            boolean ne = hasIce(blocks, row - 1, col + 1);
            boolean se = hasIce(blocks, row + 1, col + 1);
            boolean sw = hasIce(blocks, row + 1, col - 1);
            boolean nw = hasIce(blocks, row - 1, col - 1);

            return getCenterWithInnerCorners(ne, se, sw, nw);
        }

        return getBaseIceKey(mask);
    }

    private static String getBaseIceKey(int mask) {
        return switch (mask) {
            case 0 -> "ice_isolated";

            case 1 -> "ice_n";
            case 2 -> "ice_e";
            case 4 -> "ice_s";
            case 8 -> "ice_w";

            case 5 -> "ice_ns";
            case 10 -> "ice_ew";

            case 3 -> "ice_ne";
            case 6 -> "ice_es";
            case 12 -> "ice_sw";
            case 9 -> "ice_wn";

            case 7 -> "ice_nes";
            case 14 -> "ice_esw";
            case 13 -> "ice_swn";
            case 11 -> "ice_wne";

            case 15 -> "ice_center";

            default -> "ice";
        };
    }

    private static String getCenterWithInnerCorners(
            boolean ne,
            boolean se,
            boolean sw,
            boolean nw
    ) {
        boolean innerNE = !ne;
        boolean innerSE = !se;
        boolean innerSW = !sw;
        boolean innerNW = !nw;

        int innerMask = 0;

        if (innerNE) {
            innerMask |= 1;
        }

        if (innerSE) {
            innerMask |= 2;
        }

        if (innerSW) {
            innerMask |= 4;
        }

        if (innerNW) {
            innerMask |= 8;
        }

        return switch (innerMask) {
            case 0 -> "ice_center";

            case 1 -> "ice_inner_ne";
            case 2 -> "ice_inner_se";
            case 4 -> "ice_inner_sw";
            case 8 -> "ice_inner_nw";

            case 3 -> "ice_inner_ne_se";
            case 6 -> "ice_inner_se_sw";
            case 12 -> "ice_inner_sw_nw";
            case 9 -> "ice_inner_nw_ne";

            case 5 -> "ice_inner_ne_sw";
            case 10 -> "ice_inner_nw_se";

            case 7 -> "ice_inner_ne_se_sw";
            case 14 -> "ice_inner_se_sw_nw";
            case 13 -> "ice_inner_sw_nw_ne";
            case 11 -> "ice_inner_nw_ne_se";

            case 15 -> "ice_inner_all";

            default -> "ice_center";
        };
    }

    private static boolean hasIce(Block[][] blocks, int row, int col) {
        if (row < 0 || row >= blocks.length) {
            return false;
        }

        if (col < 0 || col >= blocks[0].length) {
            return false;
        }

        return isIce(blocks[row][col]);
    }

    private static boolean isIce(Block block) {
        if (block == null) {
            return false;
        }

        return block.getType() == TileType.ICE;
    }
}