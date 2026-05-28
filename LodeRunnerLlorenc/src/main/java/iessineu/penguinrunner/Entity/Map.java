/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity;

import java.io.Serializable;

/**
 *
 * @author Marc Mas
 */
public class Map implements Serializable {

    private int level;
    private final String[] map;

    public Map(int level, String[] map) {
        this.level = level;
        this.map = map;
    }

    public String[] getMap() {
        return map;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

}
