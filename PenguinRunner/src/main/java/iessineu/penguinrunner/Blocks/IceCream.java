/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Blocks;

/**
 *
 * @author loren
 */
public class IceCream extends Block {

    public IceCream(int row, int col) {
        super(row, col, TileType.STAIR);
    }

    @Override
    public boolean isCollectable() {
        return true;
    }
}
