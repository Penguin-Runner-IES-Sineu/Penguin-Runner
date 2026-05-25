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
public class DefaultMaps {

    private static String[] map1 = {
        "####################",
        "#n.pnnnnnnnngnnnnnn#",
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
    private static String map2[] = {
        "################################",
        "#..............................#",
        "#.............G................#",
        "#..............................#",
        "#......H...............E.......#",
        "#..............................#",
        "#...........L..................#",
        "#..............................#",
        "############################"};

    static List<String[]> maps = new ArrayList<>();

    public DefaultMaps() {
        maps.add(map1);
        maps.add(map2);
    }
    
//    public List<String[]> getMaps (){
//        return maps;
//    }
    
    
    public int getAmountOfMaps(){
        return maps.size();
    }
    
    public String[] getMap(int nivell){
        return maps.get(nivell);
    }
}
