/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity.Enemies;

import java.io.Serializable;

/**
 *
 * @author loren
 */
public final class SeekerEnemy extends Enemy implements Serializable {

    private int[][] pathMap;
    
    public SeekerEnemy(int row, int col, int respawnRow, int respawnCol) {
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
        super.setPrintables("seekerenemy");
    }
}