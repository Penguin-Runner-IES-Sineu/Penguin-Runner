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
public class MenuFrame extends JFrame implements Serializable {

    private MenuPanel menu;
    private MenuFrame optionsFrame;

    public boolean arrancarJoc = false;
    public boolean forceStart = false;
    Timer timer;

    private final boolean deathMenu;

    public MenuFrame(boolean deathMenu) {
        this.deathMenu = deathMenu;

        setTitle("PenguinRunner");

        // Quan tanquem la finestra, el programa acaba.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setResizable(false);

        menu = new MenuPanel(this, deathMenu);

        add(menu);
        pack();
        setLocationRelativeTo(null);

        timer = new Timer(100, e -> actualitzarMenu());
        timer.start();
    }

    private void actualitzarMenu() {
        /*
         * Si un altre lloc força l'inici del joc,
         * tancam el menú i arrancam directament.
         */
        if (forceStart) {
            tancarMenu();
            arrancarJoc = true;
            return;
        }

        /*
         * Si el MenuPanel ha demanat obrir opcions,
         * obrim un altre MenuFrame d'opcions.
         */
        if (menu.volObrirOpcions()) {
            menu.resetObrirOpcions();
            obrirMenuOpcions();
            return;
        }

        /*
         * Si el menú principal ja no està obert,
         * vol dir que s'ha pitjat "Començar partida"
         * o "Tornar a intentar".
         */
        arrancarJoc = !menu.isOpen();

        if (arrancarJoc) {
            tancarMenu();
        }
    }

    private void obrirMenuOpcions() {
        /*
         * Evitam obrir moltes finestres d'opcions a la vegada.
         */
        if (optionsFrame != null && optionsFrame.isVisible()) {
            optionsFrame.toFront();
            return;
        }

        optionsFrame = new MenuFrame(false);

        /*
         * Convertim aquest MenuFrame en menú d'opcions.
         * Això ho farà el MenuPanel intern.
         */
        optionsFrame.convertirEnMenuOpcions();

        optionsFrame.setVisible(true);
    }

    private void convertirEnMenuOpcions() {
        remove(menu);

        menu = new MenuPanel(this, false);
        menu.convertirEnMenuOpcions();

        add(menu);
        pack();
        setLocationRelativeTo(null);

        revalidate();
        repaint();
    }

    private void tancarMenu() {
        if (timer != null) {
            timer.stop();
        }

        remove(menu);
        revalidate();
        repaint();
    }

    public boolean arrancar() {
        return arrancarJoc;
    }

    public void forceStart() {
        forceStart = true;
    }
}