/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity.enemies;

/**
 *
 * @author loren
 */
import iessineu.penguinrunner.GamePanel;
import java.util.List;
import java.util.Map;

public class IceCreamEnemy extends Enemy {

    private boolean hasIceCream = false;

    public IceCreamEnemy(int row, int col, int respawnRow, int respawnCol) {
        super(row, col, respawnRow, respawnCol);
        this.setPrintables();
    }

    public boolean hasIceCream() {
        return hasIceCream;
    }

    public void collectIceCream() {
        hasIceCream = true;
    }

    public void dropIceCream() {
        hasIceCream = false;
    }

    @Override
    public void setPrintables() {
        Map<String, List<String>> mapaSprites = GamePanel.getSpriteMap();
        List<String> atributs = mapaSprites.get("enemyIceCream");

        if (atributs != null) {
            this.setEmoji(atributs.get(0));
            this.setColorFromHex(atributs.get(1));
        } else {
            this.setEmoji("🍦");
        }
    }
}