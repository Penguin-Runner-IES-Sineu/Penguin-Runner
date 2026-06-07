/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity.enemies;

/**
 *
 * @author loren
 */
public class AmbushingEnemy extends Enemy {

    private int[][] pathMap;

    public AmbushingEnemy(int row, int col, int respawnRow, int respawnCol) {
        super(row, col, respawnRow, respawnCol);

        setPrintables();
    }

    public int[][] getPathMap() {
        return pathMap;
    }

    public void setPathMap(int[][] pathMap) {
        this.pathMap = pathMap;
    }

    @Override
    public void setPrintables() {
        super.setPrintables("ambushingenemy");
    }
}
