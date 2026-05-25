/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author loren
 */
public class CrearMapa {

    String[] mapa = {
        "####################",
        "#n.pnnnnnnnngnnnnnnnn#",
        "#n.......nnnn....nn#",
        "#nnnnnn.nnEnnnnHnnn#",
        "#nn.Gnn.n---nGnH.nn#",
        "#.....n........H.nn#",
        "#nnnnnn.nnnnnnnH.nn#",
        "#nnEnnn.nnn....Hnnn#",
        "#nnnnnn.nnnnngnH---#",
        "#nn............Hnnn#",
        "#nnnn---nnnn.nnHnnn#",
        "#PnnnHnnnnnnnnnHnnn#",
        "#..................#",
        "####################"
    };
    String mapa2[] = {
        "############################",
        "#..............................#",
        "#.............G................#",
        "#..............................#",
        "#......H...............E.......#",
        "#..............................#",
        "#...........L..................#",
        "#..............................#",
        "############################"};

    List<String[]> mapes = new ArrayList<>();

    public CrearMapa() {
        mapes.add(mapa);
        mapes.add(mapa2);
    }
}
