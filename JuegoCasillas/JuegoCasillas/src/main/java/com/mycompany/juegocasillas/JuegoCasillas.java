/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.juegocasillas;

/**
 *
 * @author loren
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JuegoCasillas extends JPanel implements KeyListener {

    static final int TILE = 48;

    String[] mapa = {
        "##########",
        "#.........#",
        "#..$......#",
        "#.........#",
        "#.........#",
        "##########"
    };

    int jugadorFila = 2;
    int jugadorCol = 2;

    int enemigoFila = 3;
    int enemigoCol = 7;

    Font fuenteEmoji;

    public JuegoCasillas() {
        setPreferredSize(new Dimension(mapa[0].length() * TILE, mapa.length * TILE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        fuenteEmoji = elegirFuenteEmoji();
    }

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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(fuenteEmoji);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        for (int fila = 0; fila < mapa.length; fila++) {
            for (int col = 0; col < mapa[fila].length(); col++) {
                char casilla = mapa[fila].charAt(col);

                int x = col * TILE;
                int y = fila * TILE;

// Fondo de la casilla
                if (casilla == '#') {
                    g2.setColor(new Color(90, 90, 90)); // pared gris
                } else {
                    g2.setColor(new Color(255, 220, 180)); // suelo claro
                }

                g2.fillRect(x, y, TILE, TILE);

// Borde de la casilla
                g2.setColor(new Color(120, 110, 90));
                g2.drawRect(x, y, TILE, TILE);

// Emoji encima
                if (casilla == '#') {
                    dibujarEmoji(g2, "🧱", fila, col, null);
                }
                if (casilla == '$') {
                    dibujarEmoji(g2, "⚽", fila, col,new Color(255, 0, 0));
                }
            }
        }

        dibujarEmoji(g2, "🐵", enemigoFila, enemigoCol, null);
        dibujarEmoji(g2, "😨", jugadorFila, jugadorCol, null);
    }

    void dibujarEmoji(Graphics2D g, String emoji, int fila, int col, Color c) {
        int x = col * TILE;
        int y = fila * TILE;

        FontMetrics fm = g.getFontMetrics();

        int textoAncho = fm.stringWidth(emoji);
        int textoAlto = fm.getAscent();

        int textoX = x + (TILE - textoAncho) / 2;
        int textoY = y + (TILE + textoAlto) / 2 - 6;
        g.setColor(c);
        g.drawString(emoji, textoX, textoY);
        
    }

    boolean esPared(int fila, int col) {
        return mapa[fila].charAt(col) == '#';
    }

    void moverJugador(int df, int dc) {
        int nuevaFila = jugadorFila + df;
        int nuevaCol = jugadorCol + dc;

        if (!esPared(nuevaFila, nuevaCol)) {
            jugadorFila = nuevaFila;
            jugadorCol = nuevaCol;
        }
    }

    void moverEnemigo() {
        int df = 0;
        int dc = 0;

        if (enemigoFila < jugadorFila) {
            df = 1;
        } else if (enemigoFila > jugadorFila) {
            df = -1;
        } else if (enemigoCol < jugadorCol) {
            dc = 1;
        } else if (enemigoCol > jugadorCol) {
            dc = -1;
        }

        int nuevaFila = enemigoFila + df;
        int nuevaCol = enemigoCol + dc;

        if (!esPared(nuevaFila, nuevaCol)) {
            enemigoFila = nuevaFila;
            enemigoCol = nuevaCol;
        }
    }

    void turno(int df, int dc) {
        moverJugador(df, dc);
        moverEnemigo();

        if (jugadorFila == enemigoFila && jugadorCol == enemigoCol) {
            JOptionPane.showMessageDialog(this, "Te atrapó el enemigo");

            jugadorFila = 2;
            jugadorCol = 2;
            enemigoFila = 3;
            enemigoCol = 7;
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            turno(-1, 0);
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            turno(1, 0);
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            turno(0, -1);
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            turno(0, 1);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame ventana = new JFrame("Juego con emojis");
        JuegoCasillas juego = new JuegoCasillas();

        ventana.add(juego);
        ventana.pack();
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);

        juego.requestFocusInWindow();
    }
}
