/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Blocks;

/**
 *
 * @author loren
 */

public class Molten extends Block {

    public Molten(int row, int col) {
        super(row, col, TileType.MOLTEN);
    }

    @Override
    public boolean isDeadlyForEnemy() {
        return true;
    }
}
