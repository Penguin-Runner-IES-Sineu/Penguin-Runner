/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

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
    private Font font;

    // Estat del joc.
    private GameState gameState;

    public GamePanel() {

        font = new Font("Segoe UI Emoji", Font.PLAIN, 30); // per defecte s'empra aquesta, i després llegim l'arxiu 

        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File("resources/font.ttf")).deriveFont(30f);
        } catch (FontFormatException | IOException ex) {
            System.out.println("Error obrint la font!");
            System.getLogger(GamePanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        gameState = new GameState();

        int width = gameState.getCols() * TILE_SIZE;
        int height = gameState.getRows() * TILE_SIZE;
       

        setPreferredSize(new Dimension(width, height + 100));
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

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP ->
                direction = Direction.UP;
            case KeyEvent.VK_DOWN ->
                direction = Direction.DOWN;
            case KeyEvent.VK_LEFT ->
                direction = Direction.LEFT;
            case KeyEvent.VK_RIGHT ->
                direction = Direction.RIGHT;
            case KeyEvent.VK_Q ->
                gameState.breakDownLeft();
            case KeyEvent.VK_E ->
                gameState.breakDownRight();
            case KeyEvent.VK_P ->
                guardarPartida();
            case KeyEvent.VK_O ->
                carregarPartida();
            case KeyEvent.VK_F ->
                gameState.interact();
        }

        gameState.takeTurn(direction);

        long start = System.currentTimeMillis();
        while (gameState.shouldDrop()) {
            long deltaTime = System.currentTimeMillis() - start;
            if (deltaTime > 500) {
                repaint();
                gameState.takeTurn(Direction.DOWN);
                start = System.currentTimeMillis();
            } else {
                System.out.println("Esperant " + System.currentTimeMillis());
                System.out.println("Delta " + deltaTime);
            }
        }
        repaint();
    }

    public void guardarPartida() {
        String nomArxiu = JOptionPane.showInputDialog("Introdueixi el nom de la partida (sense extensió) o deixa-ho buit per emprar un nom generic");
        if (nomArxiu.length() == 0) {
            int saveAmount = new File("saves/").list().length;
            nomArxiu = "partidaGuardada" + saveAmount;
        }
        GameState estat = this.gameState;
        ObjectOutputStream file;
        try {
            file = new ObjectOutputStream(new FileOutputStream("saves/" + nomArxiu + ".milm"));
            file.writeObject((Object) estat);
            File f = new File("saves/" + nomArxiu);
            System.out.println("Guardat a " + f.getAbsolutePath() + ".milm");
        } catch (IOException ex) {
            System.out.println("Problema al guardar");
            System.getLogger(GamePanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

    }

    public void carregarPartida() {
        String nomArxiu = "";
        int test = JOptionPane.showConfirmDialog(null, "Vol carregar una Partida?", "Benvingut a Penguin Runner", JOptionPane.YES_NO_OPTION);
        JPanel jp = new JPanel();
        if (test == JOptionPane.YES_OPTION) {
            JFileChooser JFC = new JFileChooser("saves/");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Arxius MILM", "milm");
            JFC.setFileFilter(filter);
            int returnVal = JFC.showOpenDialog(jp);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                nomArxiu = JFC.getSelectedFile().getName();
                System.out.println(nomArxiu);
            }
            try {
                ObjectInputStream file = new ObjectInputStream(new FileInputStream("saves/" + nomArxiu));
                this.gameState = (GameState) file.readObject();
            } catch (IOException ex) {
                System.out.println("Problema al carregar!");
            } catch (ClassNotFoundException ex) {
                System.getLogger(GamePanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
    }

    /*
     * Mètode principal de dibuix.
     * Swing el crida automàticament quan cal redibuixar.
     */
    @Override
    protected void paintComponent(Graphics g
    ) {
        super.paintComponent(g);

        drawMap(g);
        drawEnemies(g);
        drawPlayer(g);
        drawHUD(g);
    }

    /*
     * Dibuixa el mapa casella per casella.
     */
    private void drawMap(Graphics g) {
        for (int row = 0; row < gameState.getRows(); row++) {
            for (int col = 0; col < gameState.getCols(); col++) {

                TileType tile = gameState.getTile(row, col);

                if (null != tile) {
                    switch (tile) {
                        case WALL ->
                            drawWall(g, row, col);
                        case ICE ->
                            drawIce(g, row, col);
                        case ICECREAM ->
                            drawIceCream(g, row, col);
                        case STAIR ->
                            drawStair(g, row, col);
                        case RAIL ->
                            drawRail(g, row, col);
                        case DOOR ->
                            drawDoor(g, row, col);
                        case STONE ->
                            drawStone(g, row, col);
                        default -> {
                        }
                    }
                }
            }
        }
    }

    /*
     * Dibuixa una paret.
     */
    private void drawWall(Graphics g, int row, int col) {
        drawCellBackground(g, row, col, new Color(70, 70, 80));
        drawEmoji(g, "🧱", row, col, null, font);
    }

    private void drawStone(Graphics g, int row, int col) {
        drawCellBackground(g, row, col, new Color(20, 20, 20));
        drawEmoji(g, "🧱", row, col, null, font);
    }

    /*
     * Dibuixa una casella de gel.
     */
    private void drawIce(Graphics g, int row, int col) {
        drawCellBackground(g, row, col, new Color(170, 225, 255));
        drawEmoji(g, "🧱", row, col, null, font);
    }

    /*
     * Dibuixa una casella amb gelat.
     */
    private void drawIceCream(Graphics g, int row, int col) {
        drawEmoji(g, "🍦", row, col, new Color(255, 255, 153), font);
    }

    /*
     * Dibuixa una casella amb escala.
     */
    private void drawStair(Graphics g, int row, int col) {
        drawEmoji(g, "🪜", row, col, new Color(128, 64, 0), font);
    }

    /*
     * Dibuixa una casella amb pasarela.
     */
    private void drawRail(Graphics g, int row, int col) {
        drawEmoji(g, "—", row, col, new Color(134, 0, 179), font);
    }

    //Dibuixa una porta si el jugador compleix el objetivo
    private void drawDoor(Graphics g, int row, int col) {
        if (checkObjective()) {
            drawEmoji(g, "🚪", row, col, new Color(128, 64, 0), font);
        }
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

        drawEmoji(g, player.getAvatar(), player.getRow(), player.getCol(), player.getColor(), font);
    }
    
    

    /*
     * Dibuixa tots els enemics.
     */
    private void drawEnemies(Graphics g) {
        for (Enemy enemy : gameState.getEnemies()) {
            if (!enemy.getIsDead()) {
                drawEmoji(g, enemy.getAvatar(), enemy.getRow(), enemy.getCol(), enemy.getColor(), font);
            }
        }
    }

    //Dibuixa el HUD
    private void drawHUD(Graphics g) {
        // int alt = gameState.getCols() -3 ;
        int amp = 0;
        int alt = gameState.getRows();
        Player player = gameState.getPlayer();
        int iceCream = player.geticeCream();
        drawEmoji(g, player.getAvatar(), alt, amp + 1, new Color(0, 136, 204), font);
        drawEmoji(g, "🍦" , alt, amp + 5, new Color(255, 255, 153), font);
        drawEmoji(g, "" + iceCream , alt, amp + 6, new Color(255, 255, 153), font);
        drawEmoji(g, " / ", alt, amp + 7, new Color(255, 255, 153), font);
        drawEmoji(g, "" + gameState.getIceCream(), alt, amp + 8, new Color(255, 255, 153), font);
        
    }

    /*
     * Comprova que el jugador hi ha completat l'objectiu per a dibuixar la porta 
     */
    private boolean checkObjective() {
        Player player = gameState.getPlayer();
        int iceCream = player.geticeCream();
        // return true;
        return iceCream >= 1;
    }

    /*
     * Dibuixa un emoji dins una casella.
     */
    private void drawEmoji(Graphics g, String emoji, int row, int col, Color color, Font f) {
        Graphics2D g2 = (Graphics2D) g;
        g.setColor(color);
        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        g2.setFont(f);

        int cellX = col * TILE_SIZE;
        int cellY = row * TILE_SIZE;

        FontMetrics metrics = g2.getFontMetrics(f);

        int textWidth = metrics.stringWidth(emoji);
        int textHeight = metrics.getAscent();

        int x = cellX + (TILE_SIZE - textWidth) / 2;
        int y = cellY + (TILE_SIZE + textHeight) / 2 - 4;

        g2.drawString(emoji, x, y);
    }
}
