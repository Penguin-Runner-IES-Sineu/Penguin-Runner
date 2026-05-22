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
 * Classe del jugador.
 *
 * El jugador només guarda la seva posició en caselles:
 * row = fila
 * col = columna
 *
 * No guardem x/y en píxels perquè això és només per dibuixar.
 */
public class Player {

    private int row;
    private int col;

    public Player(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }
}