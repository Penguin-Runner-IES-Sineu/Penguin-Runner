/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import iessineu.penguinrunner.Entity.Items.Flamethrower;
import iessineu.penguinrunner.Entity.Items.Item;
import iessineu.penguinrunner.Entity.Items.Teleport;
import iessineu.penguinrunner.GameState;
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

        private int iceCream;
        private int lives = 3;
        private static final int MAX_LIVES = 3;
        private PlayerState state;
        private List<Item> items = new LinkedList();
        private int selectedItem = 0;
        private Direction lastDirection = Direction.RIGHT;

        public Player(int row, int col) {
            this.row = row;
            this.col = col;

            this.originalRow = row;
            this.originalCol = col;

            this.lives = MAX_LIVES;

            this.state = new WalkingState();
            this.setPrintables();
        }

        public int getLives() {
            return lives;
        }

        public void setLives(int lives) {
            this.lives = lives;
        }

        public boolean isAlive() {
            return lives > 0;
        }

        public void loseLife() {
            if (lives > 0) {
                lives--;
            }
    }

    public void setItems(List<Item> itemList) {
        this.items = itemList;
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

    public void setIceCream(int icecream) {
        this.iceCream = icecream;
    }

    public void addItem(String type) {
        switch (type) {
            case "flamethrower" -> {
                Flamethrower f = new Flamethrower();
                items.add(f);
                System.out.println("Has agafat un lanzallamas!");
            }
            case "teleport" -> {
                Teleport t = new Teleport();
                items.add(t);
                System.out.println("Has agafat un teleport!");
            }
        }
    }

    public void useItem(GameState gs) {
        Item item = items.get(selectedItem);
        switch (item.getName()) {
            case "flamethrower" -> {
                Flamethrower f = (Flamethrower) item;
                f.use(this, gs);
                if (f.expired()) {
                    items.remove(selectedItem);
                    selectedItem = 0;
                    System.out.println("L'objecte ha acabat els usos");
                }
            }
            case "teleport" -> {
                Teleport t = (Teleport) item;
            }
        }
    }

    public boolean hasItem(String type) {
        for (Item item : items) {
            if (item.getName().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public void removeItem(){
        items.remove(selectedItem);
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

    public Item getSelectedItem() {
        if (hasItems()) {
            return items.get(selectedItem);
        } else {
            return new Item(""); //retornam un element buit per evitar "item is null", no farà res igualment perque la funcio que el crida empra un switch
        }
    }

    public void nextItem() {
        this.selectedItem++;
        if (this.selectedItem >= items.size()) {
            this.selectedItem = 0;
        }
    }

    public void previousItem() {
        this.selectedItem--;
        if (this.selectedItem < 0) {
            this.selectedItem = items.size() - 1;
        }
    }

    public boolean hasItems() {
        return !items.isEmpty();
    }

    public List<Item> getItems() {
        return items;
    }
}
