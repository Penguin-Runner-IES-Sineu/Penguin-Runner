/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.Timer;

/**
 *
 * @author loren
 */

/*
 * Aquesta classe representa la finestra principal del joc.
 * Només crea la finestra i hi posa el GamePanel.
 */
public class GameFrame extends JFrame implements Serializable {

    private GamePanel attachedPanel = null;

    private boolean arrancar = false;
    Timer timer;

    public GameFrame() {
        setTitle("PenguinRunner");

        // Quan tanquem la finestra, el programa acaba.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // No deixem canviar la mida de la finestra.
        setResizable(false);

        // Afegim el panell del joc.
        // MenuPanel menu = new MenuPanel(this);
        // timer = new Timer(200, e -> arrancar(menu));
        // timer.start();
        // add(menu);
        // pack();
        // setLocationRelativeTo(null);
        // Centra la finestra a la pantalla.
        GamePanel panel = new GamePanel(this);
        add(panel);
        // add(new GamePanel(this));
        attachedPanel = panel;
        // Ajusta la mida de la finestra segons el GamePanel.
        pack();
        // Centra la finestra a la pantalla.
        setLocationRelativeTo(null);
    }

    public GamePanel getPanel() {
        return attachedPanel;
    }
}
