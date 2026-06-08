/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package iessineu.penguinrunner;

import java.util.Arrays;

import javax.swing.Timer;

/**
 *
 * @author Marc Mas, Llorenç Gayà, Maria Esperança, Iñaqui Insurralde
 */
public class PenguinRunner {

    static String[] claus = new String[0];
    static String[] valors = new String[0];
    static boolean arrancar = false;
    static Timer menuTimer;

    public static void main(String[] args) {

        for (String arg : args) {
            if (arg.startsWith("-")) {
                claus = Arrays.copyOf(claus, claus.length + 1);  //si té un guió, feim es
                claus[claus.length - 1] = arg;
                valors = Arrays.copyOf(valors, valors.length + 1); //aixi cream forats a l'array de valors, si hi ha dos parametres seguits, quedarà un forat amb -1, per després donar un error
                valors[valors.length - 1] = "-1"; // -1 és un valor que donarà error quan intentem canviar el valor
            } else {
                if (valors[valors.length - 1].equals("-1")) {
                    valors[valors.length - 1] = arg; // si l'argument no és -1, vol dir que ja l'hem canviat, i no volem sobreescriure-lo
                }
            }
        }

        MenuFrame menu = new MenuFrame(false);
        menu.setVisible(true);
        for (String clau : claus) {
            if (clau.equals("-skip")) {
                menu.forceStart();
            }
        }
        menuTimer = new Timer(200, e -> arrancar(menu));
        menuTimer.start();

    }

    public static void cls() {
        for (int i = 0; i < 100; i++) {
            System.out.println("");
        }
    }

    public static void arrancar(MenuFrame menu) {
        arrancar = menu.arrancar();
        if (arrancar) {
            menuTimer.stop();
            menu.setVisible(false);

            GameFrame frame = null;
            for (int i = 0; i < claus.length; i++) {
                String clau = claus[i];
                String seleccio = valors[i];
                switch (clau) {
                    case "-g", "-game" -> {
                        GamePanel.setFolderPath("games/" + seleccio + "/");
                        GamePanel.setGame(true);
                        System.out.println("Carregant joc personalitzat...");
                        cls();
                        GamePanel.updatePaths();
                        GamePanel.setSpriteMap(GamePanel.createSpriteMap(false));
                    }
                    case "-m", "-mod" -> {
                        if (frame == null) {
                            frame = new GameFrame();
                        }
                        System.out.println("Carregant mods...");
                        GamePanel panel = frame.getPanel();
                        panel.loadMods("mods/" + seleccio);
                    }
                }
            }

            if (frame == null) {
                frame = new GameFrame();
            }

            frame.setVisible(true);
        }
    }
}
