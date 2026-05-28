/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

import java.io.Serializable;

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
public class Player implements Serializable {

    private String avatar = "🐧";
    private int row;
    private int col;
    private int iceCream = 0;

    public Player(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void addIceCream() {
        this.iceCream++;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int geticeCream() {
        return iceCream;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
