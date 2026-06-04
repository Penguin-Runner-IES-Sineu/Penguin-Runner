/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import iessineu.penguinrunner.Entity.Items.Flamethrower;
import iessineu.penguinrunner.Entity.Items.Item;
import iessineu.penguinrunner.Movement.Direction;
import iessineu.penguinrunner.Printable;
import iessineu.penguinrunner.States.PlayerState;
import iessineu.penguinrunner.States.WalkingState;

/**
 *
 * @author loren
 */

/*
 * Classe del jugador.
 *
 * El jugador només guarda la seva posició en caselles:
 * row = fila
 * col = columna
 *
 * No guardem x/y en píxels perquè això és només per dibuixar.
 */
public class Player extends Printable implements Serializable {

    private int row;
    private int col;

    private final int originalRow;
    private final int originalCol;

    private int iceCream = 0;

    private PlayerState state;
    private List<Item> items = new ArrayList();
    private Direction lastDirection = Direction.RIGHT;

    public Player(int row, int col) {
        this.row = row;
        this.col = col;

        this.originalRow = row;
        this.originalCol = col;

        this.state = new WalkingState();
        this.setPrintables();
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void moveToOriginalPosition() {
        this.row = originalRow;
        this.col = originalCol;
    }

    public int getOriginalRow() {
        return originalRow;
    }

    public int getOriginalCol() {
        return originalCol;
    }

    public void addIceCream() {
        iceCream++;
    }

    public int geticeCream() {
        return iceCream;
    }

    public void addItem(String type) {
        switch (type) {
            case "flamethrower" -> {
                Flamethrower f = new Flamethrower();
                items.add(f);
                System.out.println("Has agafat un lanzallamas!");
            }
            case "teleport" -> {

            }
        }
    }

    public void useItem(String type) {
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.getName().equals(type)) {
                switch (type) {
                    case "flamethrower" -> {
                        Flamethrower f = (Flamethrower) item;
                        f.use(this.lastDirection);
                        // items.remove(i);
                    }
                    case "teleport" -> {

                    }
                }
            }
        }
    }

    public PlayerState getState() {
        return state;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public void setPrintables() {
        super.setPrintables("player");
    }

    public Direction getLastDirection() {
        return lastDirection;
    }

    public void setLastDirection(Direction lastDirection) {
        this.lastDirection = lastDirection;
    }
}
