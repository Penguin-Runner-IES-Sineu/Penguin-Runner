/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Blocks;

/**
 *
 * @author loren
 */

public class Stone extends Block {

    public Stone(int row, int col) {
        super(row, col, TileType.STONE);
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }
}
