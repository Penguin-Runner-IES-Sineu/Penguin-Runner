/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity;

import iessineu.penguinrunner.States.WalkingState;
import iessineu.penguinrunner.States.PlayerState;
import java.awt.Color;
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

    private int row;
    private int col;

    private final int originalRow;
    private final int originalCol;

    private int iceCream = 0;

    private PlayerState state;

    public Player(int row, int col) {
        this.row = row;
        this.col = col;

        this.originalRow = row;
        this.originalCol = col;

        this.state = new WalkingState();
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

    public void moveToOriginalPosition() {
        this.row = originalRow;
        this.col = originalCol;
    }

    public int getOriginalRow() {
        return originalRow;
    }

    public int getOriginalCol() {
        return originalCol;
    }

    public void addIceCream() {
        iceCream++;
    }

    public int geticeCream() {
        return iceCream;
    }

    public PlayerState getState() {
        return state;
    }
    public String getAvatar() {
        return "🐧";
    }
    public Color getColor() {
        return new Color(71, 71, 209);
    }

    public void setState(PlayerState state) {
        this.state = state;
    }
}