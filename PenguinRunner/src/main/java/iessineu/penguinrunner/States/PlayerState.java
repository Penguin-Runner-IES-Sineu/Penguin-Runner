/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.States;

/**
 *
 * @author loren
 */
import java.io.Serializable;

import iessineu.penguinrunner.GameState;
import iessineu.penguinrunner.Movement.Direction;

public interface PlayerState extends Serializable {

    void handleInput(GameState gameState, Direction direction);

    String getName();
}

