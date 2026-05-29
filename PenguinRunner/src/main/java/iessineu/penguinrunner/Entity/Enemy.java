/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity;

import java.awt.Color;
import java.io.Serializable;

import iessineu.penguinrunner.Printable;

/**
 *
 * @author loren
 */

/*
 * Classe d'un enemic.
 *
 * Igual que el jugador, l'enemic es mou per caselles.
 */
public class Enemy extends Printable implements Serializable {

    private int row;
    private int col;
    private final int respawnRow;
    private final int respawnCol;
    private final int originalRow;
    private final int originalCol;

    private boolean isDead = false;
    private int timeToRevive = 0;

    public Enemy(int row, int col, int respawnRow, int respawnCol) {
        originalRow = this.row = row;
        originalCol = this.col = col;
        this.respawnRow = respawnRow;
        this.respawnCol = respawnCol;
    }

    public int getRow() {
        return row;
    }

    public int respawnRow() {
        return respawnRow;
    }

    public void moveToOriginalRow() {
        row = originalRow;
        col = originalCol;
    }

    public int getCol() {
        return col;
    }

    public int getRespawnCol() {
        return respawnCol;
    }

    public boolean getIsDead() {
        return isDead;
    }

    public void revive() {
        isDead = false;
        setPosition(respawnRow, respawnCol);
    }

    public void die() {
        isDead = true;
    }

    public void setTimeToRevive(int timeToRevive) {
        this.timeToRevive = timeToRevive;
    }

    public int getTimeToRevive() {
        return timeToRevive;
    }

    public void subtractTimeToRevive(int timeLess) {
        this.timeToRevive -= timeLess;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public String getAvatar() {
        return "🦭";
    }

    public Color getColor() {
        return new Color(26, 140, 255);
    }

}
