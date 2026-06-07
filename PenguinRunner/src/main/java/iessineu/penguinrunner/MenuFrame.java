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

    public boolean arrancarJoc = false;
    public boolean forceStart = false;
    Timer timer;

    public MenuFrame() {
        setTitle("PenguinRunner");

        // Quan tanquem la finestra, el programa acaba.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setResizable(false);

        menu = new MenuPanel(this);
        timer = new Timer(200, e -> arrancar());
        timer.start();
        add(menu);
        pack();
        setLocationRelativeTo(null);
        
    }

    public boolean arrancar() {
        if(forceStart){
            remove(menu);
            return true;
        }
        arrancarJoc = !menu.isOpen();
        if (arrancarJoc) {
            remove(menu);
        }
        return arrancarJoc;
    }

    public void forceStart(){
        forceStart = true;
    }

}
