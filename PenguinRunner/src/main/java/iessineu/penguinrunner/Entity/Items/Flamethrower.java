/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity.Items;

import java.io.Serializable;

import iessineu.penguinrunner.Movement.Direction;

public class Flamethrower extends Item implements Serializable {

    public Flamethrower() {
        super("flamethrower", 3);
    }

    public void use(Direction direction) {
        System.out.println("Llamas cap a " + direction);
        usesLeft--;
        System.out.println("Te queden " + usesLeft + " usos");
    }

    public boolean expired(){
        return usesLeft <= 0;
    }
}
