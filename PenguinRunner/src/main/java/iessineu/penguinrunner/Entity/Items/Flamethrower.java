/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity.Items;

import java.io.Serializable;

import iessineu.penguinrunner.Movement.Direction;

public class Flamethrower extends Item implements Serializable {

    public Flamethrower() {
        super("flamethrower");
    }

    public void use(Direction direction) {
        System.out.println("Llamas cap a " + direction);
    }
}
