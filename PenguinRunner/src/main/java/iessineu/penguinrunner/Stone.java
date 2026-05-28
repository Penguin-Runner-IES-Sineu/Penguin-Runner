/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

/**
 *
 * @author loren
 */
public class Stone {
    private int row;
    private int col;
    private final int originalRow;
    private final int originalCol;
    public Stone(int row, int col){
        originalRow = this.row = row;
        originalCol = this.col = col;
    }
    
    public void setPosition(int row, int col){
        this.row = row;
        this.col = col;
    }
    
    public final int getRow(){
        return row;
    }
    public final int getCol(){
        return col;
    }
    public final int getOriginalRow(){
        return originalRow;
    }
    public final int getOriginalCol(){
        return originalCol;
    }
    public void moveToOriginalRow(){
        row = originalRow;
        col = originalCol;
    }
    
}
