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
public class WalkingState implements PlayerState {

    @Override
    public void handleInput(GameState gameState, Direction direction) {
        if (direction == null) {
            return;
        }

        // Caminant normal no pot pujar si no està a una escala
        if (direction == Direction.UP) {
            return;
        }

        // Pot baixar si la casella és valida
        if (direction == Direction.DOWN) {
            gameState.movePlayerBy(direction, false);
            return;
        }

        // Esquerra/dreta: pot moure pedres
        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            gameState.movePlayerBy(direction, true);
        }
    }

    @Override
    public String getName() {
        return "WALKING";
    }
}