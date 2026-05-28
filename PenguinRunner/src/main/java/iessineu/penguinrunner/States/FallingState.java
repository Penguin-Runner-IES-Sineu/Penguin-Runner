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

public class FallingState implements PlayerState {

    @Override
    public void handleInput(GameState gameState, Direction direction) {
        // Quan cau, ignora la tecla i baixa una casella.
        gameState.movePlayerDownOne();
    }

    @Override
    public String getName() {
        return "FALLING";
    }
}