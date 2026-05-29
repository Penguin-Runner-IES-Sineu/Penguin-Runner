/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Blocks;

/**
 *
 * @author loren
 */
public class Wall extends Block {

    public Wall(int row, int col) {
        super(row, col, TileType.WALL);
    }

    @Override
    public boolean isSolid() {
        return true;
    }
}