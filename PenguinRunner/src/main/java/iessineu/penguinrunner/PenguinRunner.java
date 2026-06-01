/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package iessineu.penguinrunner;

import javax.swing.SwingUtilities;

/**
 *
 * @author loren
 */
public class PenguinRunner {

    static GameFrame frame;
    static GameState state;

    // public static void init() {
    //     frame = new GameFrame();
    //     frame.setVisible(true);
    // }

    public static void main(String[] args) {
        
        /*
         * SwingUtilities.invokeLater fa que la finestra es creï
         * correctament dins el fil d'execució de Swing.
         */
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });

        // init();
        // while (true) {
        //     update();
        //     repaint();
        // }
    }

    // public static void update() {
    //     state = frame.getGameState();
    //     state.updateLogic();
    // }

    // public static void repaint() {
    //     frame.repaint();
    // }
}
