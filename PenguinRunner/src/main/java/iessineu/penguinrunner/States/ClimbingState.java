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

public class ClimbingState implements PlayerState {

    @Override
    public void handleInput(GameState gameState, Direction direction) {
        if (direction == null) {
            return;
        }

        // En una escala pot pujar i baixar
        if (direction == Direction.UP || direction == Direction.DOWN) {
            gameState.movePlayerBy(direction, false);
            return;
        }

        // També pot sortir lateralment de l'escala
        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            gameState.movePlayerBy(direction, true);
        }
    }

    @Override
    public String getName() {
        return "CLIMBING";
    }
}