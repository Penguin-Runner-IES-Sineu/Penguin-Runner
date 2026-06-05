/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package iessineu.penguinrunner;

import java.util.Arrays;

/**
 *
 * @author loren
 */
public class PenguinRunner {

    public static void main(String[] args) {

        String[] claus = new String[0];
        String[] valors = new String[0];
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

        GameFrame frame = null;
        // GameFrame frame = new GameFrame();
        for (int i = 0; i < claus.length; i++) {
            String clau = claus[i];
            if ("v".equals(clau)) { //parametre -v per carregar vanilla si o si
                break;
            }
            String seleccio = valors[i];
            switch (clau) {
                case "-g", "-game" -> {
                    System.out.println("Carregant joc personalitzat...");
                    GamePanel.setFolderPath("games/" + seleccio + "/");
                    cls();
                    GamePanel.updatePaths();
                    GamePanel.setGame(true);
                    GamePanel.setSpriteMap(GamePanel.createSpriteMap(false));
                    frame = new GameFrame();
                }
                case "-m", "-mod" -> {
                    frame = new GameFrame();
                    System.out.println("Carregant mods...");
                    GamePanel panel = frame.getPanel();
                    panel.loadMods("mods/" + seleccio);
                }
            }
        }
        /*
         * SwingUtilities.invokeLater fa que la finestra es creï
         * correctament dins el fil d'execució de Swing.
         */
        // SwingUtilities.invokeLater(() -> {
        // GamePanel.setFolderPath("");
        // GamePanel.setFolderPath("resources/");
        // GamePanel.setGame(false);
        if (frame == null) {
            frame = new GameFrame();
        }
        frame.setVisible(true);
        // });

    }

    public static void cls() {
        for (int i = 0; i < 100; i++) {
            System.out.println("");
        }
    }
}
