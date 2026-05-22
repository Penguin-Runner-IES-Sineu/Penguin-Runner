/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.loderunnerllorenc;

/**
 *
 * @author loren
 */

/*
 * Classe d'un enemic.
 *
 * Igual que el jugador, l'enemic es mou per caselles.
 */
public class Enemy {

    private int row;
    private int col;
    private int rowOriginal;
    private int colOriginal;

    public Enemy(int row, int col) {
        rowOriginal = this.row = row;
        colOriginal = this.col = col;
    }

    public int getRow() {
        return row;
    }
    public int getRowOriginal() {
        return rowOriginal;
    }
    

    public int getCol() {
        return col;
    }
    public int getColOriginal() {
        return colOriginal;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }
}