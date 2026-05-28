/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.States;

import iessineu.penguinrunner.Movement.Direction;
import iessineu.penguinrunner.GameState;

/**
 *
 * @author loren
 */
public class RailState implements PlayerState {

    @Override
    public void handleInput(GameState gameState, Direction direction) {
        if (direction == null) {
            return;
        }

        // A una passarel·la pot anar esquerra/dreta
        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            gameState.movePlayerBy(direction, true);
            return;
        }

        // Pot baixar si hi ha espai
        if (direction == Direction.DOWN) {
            gameState.movePlayerBy(direction, false);
            return;
        }

        // Només pot pujar si també està sobre una escala
        if (direction == Direction.UP && gameState.isPlayerOnStair()) {
            gameState.movePlayerBy(direction, false);
        }
    }

    @Override
    public String getName() {
        return "ON_RAIL";
    }
}