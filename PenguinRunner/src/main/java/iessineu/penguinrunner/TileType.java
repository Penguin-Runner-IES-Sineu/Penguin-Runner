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
 * Tipus de caselles que pot tenir el mapa.
 */
public enum TileType implements Serializable {
    ICE,
    WALL,
    ICECREAM,
    STAIR,
    RAIL,
    MOLTEN,
    DOOR,
    STONE,
    BLANK
}