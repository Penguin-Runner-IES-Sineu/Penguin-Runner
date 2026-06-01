/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

import javax.swing.JFrame;

/**
 *
 * @author loren
 */

/*
 * Aquesta classe representa la finestra principal del joc.
 * Només crea la finestra i hi posa el GamePanel.
 */
public class GameFrame extends JFrame {

    // private GameState state = null;
    public GameFrame() {
        setTitle("PenguinRunner");

        // Quan tanquem la finestra, el programa acaba.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // No deixem canviar la mida de la finestra.
        setResizable(false);

        // Afegim el panell del joc.
        GamePanel panel = new GamePanel(this);
        add(panel);
        // add(new GamePanel(this));
        // state = panel.getGameState();

        // Ajusta la mida de la finestra segons el GamePanel.
        pack();

        // Centra la finestra a la pantalla.
        setLocationRelativeTo(null);
    }

    // public GameState getGameState() {
    //     return state;
    // }
}
