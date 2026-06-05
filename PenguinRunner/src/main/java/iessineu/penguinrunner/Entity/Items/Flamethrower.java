/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity.Items;

import java.io.Serializable;

import iessineu.penguinrunner.Blocks.Block;
import iessineu.penguinrunner.Blocks.TileType;
import iessineu.penguinrunner.Entity.Player;
import iessineu.penguinrunner.GameState;
import iessineu.penguinrunner.Movement.Direction;

public class Flamethrower extends Item implements Serializable {

    int usesLeft;

    public Flamethrower() {
        super("flamethrower");
        this.usesLeft = 3;
    }

    public void use(Player p, GameState gs) {
        System.out.println("Llamas cap a " + p.getLastDirection());
        usesLeft--;
        int playerRow = p.getRow();
        int playerCol = p.getCol();
        switch (p.getLastDirection()) {
            case Direction.UP -> {
                gs.breakBlock(playerRow - 1, playerCol + 1);
                gs.breakBlock(playerRow - 2, playerCol + 1);
                gs.breakBlock(playerRow - 3, playerCol + 1);
                Block[][] map = gs.getBlocks();
                Block b;
                b = map[playerRow - 1][playerCol + 1];
                burnBlock(b);
                b = map[playerRow - 2][playerCol + 1];
                burnBlock(b);
                b = map[playerRow - 3][playerCol + 1];
                burnBlock(b);
            }
            case Direction.DOWN -> {
                gs.breakBlock(playerRow + 1, playerCol + 1);
                gs.breakBlock(playerRow + 2, playerCol + 1);
                gs.breakBlock(playerRow + 3, playerCol + 1);
            }
            case Direction.LEFT -> {
                gs.breakBlock(playerRow + 1, playerCol - 1);
                gs.breakBlock(playerRow + 1, playerCol - 2);
                gs.breakBlock(playerRow + 1, playerCol - 3);
                Block[][] map = gs.getBlocks();
                Block b;
                b = map[playerRow + 1][playerCol - 1];
                burnBlock(b);
                b = map[playerRow + 1][playerCol - 2];
                burnBlock(b);
                b = map[playerRow - +1][playerCol - 3];
                burnBlock(b);
            }
            case Direction.RIGHT -> {
                gs.breakBlock(playerRow + 1, playerCol + 1);
                gs.breakBlock(playerRow + 1, playerCol + 2);
                gs.breakBlock(playerRow + 1, playerCol + 3);
            }
        }
        System.out.println("Te queden " + usesLeft + " usos");
    }

    public boolean expired() {
        return usesLeft <= 0;
    }

    public void burnBlock(Block b) {
        b.setType(TileType.BURNT);
        b.setPrintables();
        b.setType(TileType.MOLTEN);
    }
}
