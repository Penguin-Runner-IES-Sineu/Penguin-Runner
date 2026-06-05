/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner.Entity.Items;

import java.io.Serializable;

import iessineu.penguinrunner.Printable;

public class Item extends Printable implements Serializable {

    String name;
    int usesLeft;

    public Item(String name, int usesLeft) {
        this.name = name;
        this.usesLeft = usesLeft;
    }

    public String getName() {
        return name;
    }
}
