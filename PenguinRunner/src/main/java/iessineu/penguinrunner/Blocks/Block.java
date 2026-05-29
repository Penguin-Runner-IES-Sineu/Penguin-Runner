/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Blocks;

/**
 *
 * @author loren
 */
import java.awt.Color;
import java.io.Serializable;

import iessineu.penguinrunner.Printable;

public class Block extends Printable implements Serializable {

    protected int row;
    protected int col;

    protected final int originalRow;
    protected final int originalCol;

    protected String sprite = "";
    protected Color color = new Color(0);

    protected final TileType type;

    public Block(int row, int col, TileType type) {
        this.row = row;
        this.col = col;
        this.originalRow = row;
        this.originalCol = col;
        this.type = type;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public TileType getType() {
        return type;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void moveToOriginalPosition() {
        this.row = originalRow;
        this.col = originalCol;
    }

    public boolean isSolid() {
        return false;
    }

    public boolean isBreakable() {
        return false;
    }

    public boolean isPushable() {
        return false;
    }

    public boolean isClimbable() {
        return false;
    }

    public boolean isRail() {
        return false;
    }

    public boolean isCollectable() {
        return false;
    }

    public boolean isDeadlyForEnemy() {
        return false;
    }
}
