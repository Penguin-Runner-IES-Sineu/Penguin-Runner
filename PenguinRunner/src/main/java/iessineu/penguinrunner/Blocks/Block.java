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

import iessineu.penguinrunner.Printable;

public class Block extends Printable implements Serializable {

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
    private boolean isBurnable = false;
    private boolean isTrapdoor = false;
    private boolean isPressed = false;
    private boolean isFire = false;

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
            case ICECREAM, FLAMETHROWER, TELEPORT -> {
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
            case WOOD -> {
                this.isSolid = true;
                this.isPushable = true;
                this.isBurnable = true;
            }
            case DOOR -> {
                this.isDoor = true;
                this.type = TileType.BLANK;
            }
            case TRAPDOOR -> {
                this.isTrapdoor = true;
                this.type = TileType.BLANK;
            }
            case BLANK -> {
                this.isSolid = false;
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

    public boolean isBurnable() {
        return isBurnable;
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

    public void setType(TileType type) {
        this.type = type;
    }

    public void setPrintables() {
        super.setPrintables(this.getType().toString().toLowerCase());
    }

    @Override
    public void setPrintables(String path) {
        super.setPrintables(path);
    }

    public void setDoor(boolean isDoor) {
        this.isDoor = isDoor;
    }

    public boolean isTrapdoor() {
        return isTrapdoor;
    }

    public boolean isPressed() {
        return isPressed;
    }

    public void setPressed(boolean isPressed) {
        this.isPressed = isPressed;
    }

    public void setCollectable(boolean isCollectable) {
        this.isCollectable = isCollectable;
    }

    public boolean isFire() {
        return isFire;
    }

    public void setFire(boolean isFire) {
        this.isFire = isFire;
    }

    public void setSolid(boolean isSolid) {
        this.isSolid = isSolid;
    }
}
