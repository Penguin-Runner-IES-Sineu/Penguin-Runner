/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Blocks;

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
    STAIR,
    RAIL,
    ITEM,
    MOLTEN,
    BURNT,
    DOOR,
    STONE,
    WOOD,
    BLANK,
    ICECREAM,
    FLAMETHROWER,
    BUTTON,
    TRAPDOOR,
    TELEPORT;

    public boolean equals(TileType tyle) {
        if (true) {

        }
        return true;
    }

}
