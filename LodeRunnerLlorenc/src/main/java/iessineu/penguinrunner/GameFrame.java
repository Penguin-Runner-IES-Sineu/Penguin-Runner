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

    public GameFrame() {
        setTitle("PenguinRunner");

        // Quan tanquem la finestra, el programa acaba.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // No deixem canviar la mida de la finestra.
        setResizable(false);

        // Afegim el panell del joc.
        add(new GamePanel());

        // Ajusta la mida de la finestra segons el GamePanel.
        pack();

        // Centra la finestra a la pantalla.
        setLocationRelativeTo(null);
    }
}
