/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Blocks;

/**
 *
 * @author loren
 */
import java.awt.Color;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import iessineu.penguinrunner.GamePanel;
import iessineu.penguinrunner.Printable;

public class Block extends Printable implements Serializable {

    // protected int row;
    // protected int col;
    // protected final int originalRow;
    // protected final int originalCol;
    protected String sprite = "";
    protected Color color = new Color(0);

    private boolean isSolid = false;
    private boolean isBreakable = false;
    private boolean isPushable = false;
    private boolean isClimbable = false;
    private boolean isRail = false;
    private boolean isCollectable = false;
    private boolean isDeadlyForEnemy = false;
    private boolean isDoor = false;

    protected TileType type;

    public Block(TileType type) {
        this.type = type;
        switch (type) {
            case ICE -> {
                this.isSolid = true;
                this.isBreakable = true;
            }
            case WALL -> {
                this.isSolid = true;
            }
            case ICECREAM -> {
                this.isCollectable = true;
            }
            case STAIR -> {
                this.isClimbable = true;
            }
            case RAIL -> {
                this.isRail = true;
            }
            case MOLTEN -> {
                this.isDeadlyForEnemy = true;
            }
            case STONE -> {
                this.isSolid = true;
                this.isPushable = true;
            }
            case BLANK -> {
                this.isSolid = false;
            }
            case DOOR -> {
                this.isDoor = true;
                this.type = TileType.BLANK;
            }

        }
        this.setPrintables();
    }

    public TileType getType() {
        return type;
    }

    public boolean isSolid() {
        return isSolid;
    }

    public boolean isBreakable() {
        return isBreakable;
    }

    public boolean isPushable() {

        return isPushable;
    }

    public boolean isDoor() {
        return isDoor;
    }

    public boolean isClimbable() {
        return isClimbable;
    }

    public boolean isRail() {
        return isRail;
    }

    public boolean isCollectable() {
        return isCollectable;
    }

    public boolean isDeadlyForEnemy() {
        return isDeadlyForEnemy;
    }

    public void setPrintables() {
        Map<String, List<String>> mapaSprites = GamePanel.createSpriteMap();
        String tipus = this.getType().toString();
        List<String> atributs = mapaSprites.get(tipus.toLowerCase());
        if (atributs != null) {
            this.setEmoji(atributs.get(0));
            this.setColorFromHex(atributs.get(1));
            this.setSprite(atributs.get(2));
        } else {
            this.setEmoji("#");
        }
    }

    public void setType(TileType type) {
        this.type = type;
    }
}
