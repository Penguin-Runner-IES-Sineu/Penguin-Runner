/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Blocks;

/**
 *
 * @author loren
 */

public class Rail extends Block {

    public Rail(int row, int col) {
        super(row, col, TileType.RAIL);
    }

    @Override
    public boolean isRail() {
        return true;
    }
}