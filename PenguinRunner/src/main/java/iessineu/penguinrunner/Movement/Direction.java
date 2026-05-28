/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Movement;

/**
 *
 * @author loren
 */

/*
 * Direccions possibles del moviment.
 *
 * Cada direcció té:
 * dr = canvi de fila
 * dc = canvi de columna
 *
 * Exemple:
 * UP(-1, 0) vol dir:
 * fila - 1, mateixa columna.
 */
public enum Direction {

    UP(-1, 0),
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1);

    private final int dr;
    private final int dc;

    Direction(int dr, int dc) {
        this.dr = dr;
        this.dc = dc;
    }

    public int getDr() {
        return dr;
    }

    public int getDc() {
        return dc;
    }
}