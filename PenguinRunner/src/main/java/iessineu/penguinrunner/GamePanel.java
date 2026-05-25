/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;

/**
 *
 * @author loren
 */

/*
 * GamePanel és la part visual del joc.
 *
 * Responsabilitats:
 * - Dibuixar el mapa.
 * - Dibuixar el jugador.
 * - Dibuixar els enemics.
 * - Detectar les tecles.
 * - Demanar al GameState que faci un torn.
 *
 * Aquesta classe NO hauria de decidir les normes del joc.
 * Les normes estan a GameState.
 */
public class GamePanel extends JPanel {

    // Mida d'una casella en píxels.
    public static final int TILE_SIZE = 40;

    // Estat del joc.
    private final GameState gameState;

    public GamePanel() {
        gameState = new GameState();

        int width = gameState.getCols() * TILE_SIZE;
        int height = gameState.getRows() * TILE_SIZE;

        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);

        // Necessari perquè el JPanel pugui rebre tecles.
        setFocusable(true);

        // Escoltar teclat.
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e);
            }
        });
    }

    /*
     * Converteix la tecla premuda en una direcció.
     */
    private void handleInput(KeyEvent e) {
        Direction direction = null;
        if(gameState.shouldDrop()){
            gameState.applyGravity();

            repaint();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            direction = Direction.UP;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            direction = Direction.DOWN;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            direction = Direction.LEFT;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            direction = Direction.RIGHT;
        }

        if (direction != null) {
            gameState.takeTurn(direction);
            repaint();
        }
        if (e.getKeyCode() == KeyEvent.VK_Q) {
            gameState.takeTurn();
            gameState.breakDownLeft();
            repaint();
        }
        if (e.getKeyCode() == KeyEvent.VK_E) {
            gameState.takeTurn();
            gameState.breakDownRight();
            repaint();
        }
    }

    /*
     * Mètode principal de dibuix.
     * Swing el crida automàticament quan cal redibuixar.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawMap(g);
        drawEnemies(g);
        drawPlayer(g);
    }

    /*
     * Dibuixa el mapa casella per casella.
     */
    private void drawMap(Graphics g) {
        for (int row = 0; row < gameState.getRows(); row++) {
            for (int col = 0; col < gameState.getCols(); col++) {

                TileType tile = gameState.getTile(row, col);

                if (tile == TileType.WALL) {
                    drawParet(g, row, col);
                } else if (tile == TileType.GEL) {
                    drawGel(g, row, col);
                } else if (tile == TileType.GELAT) {
                    drawGelat(g, row, col);
                } else if (tile == TileType.ESCALA) {
                    drawEscala(g, row, col);
                } else if (tile == TileType.PASARELA) {
                    drawPasarela(g, row, col);
                }
            }
        }
    }

    /*
     * Dibuixa una paret.
     */
    private void drawParet(Graphics g, int row, int col) {
        drawCellBackground(g, row, col, new Color(70, 70, 80));
        drawEmoji(g, "🧱", row, col, null);
    }

    /*
     * Dibuixa una casella de gel.
     */
    private void drawGel(Graphics g, int row, int col) {
        drawCellBackground(g, row, col, new Color(170, 225, 255));
        drawEmoji(g, "🧱", row, col, null);
    }

    /*
     * Dibuixa una casella amb gelat.
     */
    private void drawGelat(Graphics g, int row, int col) {
        drawEmoji(g, "🍦", row, col, new Color (255, 255, 153));
    }

    /*
     * Dibuixa una casella amb escala.
     */
    private void drawEscala(Graphics g, int row, int col) {
        drawEmoji(g, "🪜", row, col, new Color(128, 64, 0));
    }

    /*
     * Dibuixa una casella amb pasarela.
     */
    private void drawPasarela(Graphics g, int row, int col) {
        drawEmoji(g, "—", row, col, new Color(134, 0, 179));
    }


    /*
     * Dibuixa el fons d'una casella i la quadrícula.
     */
    private void drawCellBackground(Graphics g, int row, int col, Color color) {
        int x = col * TILE_SIZE;
        int y = row * TILE_SIZE;

        g.setColor(color);
        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);

        // Vora de la casella
        g.setColor(new Color(90, 130, 150));
        g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
    }

    /*
     * Dibuixa el jugador.
     */
    private void drawPlayer(Graphics g) {
        Player player = gameState.getPlayer();

        drawEmoji(g, "🐧", player.getRow(), player.getCol(), new Color(0, 136, 204));
    }

    /*
     * Dibuixa tots els enemics.
     */
    private void drawEnemies(Graphics g) {
        for (Enemy enemy : gameState.getEnemies()) {
            if(!enemy.getIsDead()){
                drawEmoji(g, "🦭", enemy.getRow(), enemy.getCol(), null);
            }
        }
    }

    /*
     * Dibuixa un emoji dins una casella.
     */
    private void drawEmoji(Graphics g, String emoji, int row, int col, Color color) {
        Graphics2D g2 = (Graphics2D) g;
        g.setColor(color);
        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
        
        Font font = new Font("Segoe UI Emoji", Font.PLAIN, 30);
        g2.setFont(font);

        int cellX = col * TILE_SIZE;
        int cellY = row * TILE_SIZE;

        FontMetrics metrics = g2.getFontMetrics(font);

        int textWidth = metrics.stringWidth(emoji);
        int textHeight = metrics.getAscent();

        int x = cellX + (TILE_SIZE - textWidth) / 2;
        int y = cellY + (TILE_SIZE + textHeight) / 2 - 4;

        g2.drawString(emoji, x, y);
    }
}
