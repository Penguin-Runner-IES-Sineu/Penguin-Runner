/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import iessineu.penguinrunner.GamePanel;
import iessineu.penguinrunner.Printable;

/**
 *
 * @author loren
 */
public final class SeekerEnemy extends Enemy {

    private int[][] pathMap;
    
    public SeekerEnemy(int row, int col, int respawnRow, int respawnCol) {
        super(row, col, respawnRow, respawnCol);
        
        setPrintables();
    }

    public int[][] getPathMap() {
        return pathMap;
    }

    public void setPathMap(int[][] pathMap) {
        this.pathMap = pathMap;
    }

    @Override
    public void setPrintables() {
        Map<String, List<String>> mapaSprites = GamePanel.createSpriteMap();
        List<String> atributs = mapaSprites.get("enemy2");

        if (atributs != null) {
            this.setEmoji(atributs.get(0));
            this.setColorFromHex(atributs.get(1));
            this.setSprite(atributs.get(2));
        } else {
            this.setEmoji("#");
        }
    }
}