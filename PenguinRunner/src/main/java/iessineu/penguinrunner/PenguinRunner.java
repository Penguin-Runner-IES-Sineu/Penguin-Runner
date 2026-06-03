/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package iessineu.penguinrunner;

import java.util.Arrays;

import javax.swing.SwingUtilities;

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

        for (int i = 0; i < claus.length; i++) {
            String clau = claus[i];
            String seleccio = valors[i];
            switch (clau) {
                case "-g", "-game" -> {
                    GamePanel.setFolderPath(seleccio + "/");
                    GamePanel.updatePaths();
                    GamePanel.setGame(true);
                }
                case "-m", "-mod" -> {
                }
            }
        }
        /*
         * SwingUtilities.invokeLater fa que la finestra es creï
         * correctament dins el fil d'execució de Swing.
         */
        SwingUtilities.invokeLater(() -> {
            // GamePanel.setFolderPath("");
            // GamePanel.setFolderPath("resources/");
            // GamePanel.setGame(false);
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });

    }
}
