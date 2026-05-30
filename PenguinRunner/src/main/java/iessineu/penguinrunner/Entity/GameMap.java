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
public class GameMap implements Serializable {

    private final String[] map;

    public GameMap(String[] map) {
        this.map = map;
    }

    public String[] getMap() {
        return map;
    }


}
