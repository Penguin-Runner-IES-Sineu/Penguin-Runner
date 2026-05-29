/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

import java.awt.Font;

/**
 *
 * @author loren
 */
public class FontEmojis {
        Font elegirFuenteEmoji() {
        String[] fuentes = {
            "Segoe UI Emoji", // Windows
            "Apple Color Emoji", // macOS
            "Noto Color Emoji", // Linux
            "Dialog" // fallback
        };

        for (String nombre : fuentes) {
            Font f = new Font(nombre, Font.PLAIN, 34);
            if (f.canDisplay('A')) {
                return f;
            }
        }

        return new Font("Dialog", Font.PLAIN, 34);
    }
        
}
