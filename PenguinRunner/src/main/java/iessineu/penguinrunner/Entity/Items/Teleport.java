/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity.Items;

import java.io.Serializable;

import iessineu.penguinrunner.Entity.Player;

public class Teleport extends Item implements Serializable {

    boolean pointSet;
    int row;
    int col;

    public Teleport() {
        super("teleport");
    }

    private void setPoint(int row, int col) {
        this.row = row;
        this.col = col;
        pointSet = true;
    }

    public boolean use(Player p) {
        if (pointSet) {
            p.setPosition(row, col);
            return true;
        }
        setPoint(p.getRow(), p.getCol());
        return false;
    }

}
