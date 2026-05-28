/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

import iessineu.penguinrunner.Blocks.TileType;
import iessineu.penguinrunner.Entity.Enemy;
import iessineu.penguinrunner.Entity.Player;
import iessineu.penguinrunner.Movement.Direction;
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

public class GamePanel extends JPanel {

    public static final int TILE_SIZE = 40;
    private static final int HUD_HEIGHT = 100;


    private Font font;

    private GameState gameState;

    public GamePanel() {
        
        font = new Font("Segoe UI Emoji", Font.PLAIN, 30); // per defecte s'empra aquesta, i després llegim l'arxiu 

        this.gameState = new GameState();

        resizePanelToGame();

        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e);
            }
        });
    }

    /*
     * Carrega la font externa. Si falla, usa una font del sistema.
     */
    private Font loadfont() {

        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File("resources/font.ttf")).deriveFont(30f);
        } catch (FontFormatException | IOException ex) {
            System.out.println("Error obrint la font!");
            System.getLogger(GamePanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return font;
    }

    /*
     * Ajusta la mida del panell al mapa actual.
     * És útil també després de carregar una partida.
     */
    private void resizePanelToGame() {
        int width = gameState.getCols() * TILE_SIZE;
        int height = gameState.getRows() * TILE_SIZE + HUD_HEIGHT;

        setPreferredSize(new Dimension(width, height));
        revalidate();
    }

    /*
     * Control de teclat.
     *
     * Fletxes = moviment
     * Q = trencar abaix-esquerra
     * E = trencar abaix-dreta
     * F = interactuar
     * P = guardar
     * O = carregar
     * Espai = passar torn
     */
    private void handleInput(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                playTurn(Direction.UP);
                break;

            case KeyEvent.VK_DOWN:
                playTurn(Direction.DOWN);
                break;

            case KeyEvent.VK_LEFT:
                playTurn(Direction.LEFT);
                break;

            case KeyEvent.VK_RIGHT:
                playTurn(Direction.RIGHT);
                break;

            case KeyEvent.VK_SPACE:
                playTurn(null);
                break;

            case KeyEvent.VK_Q:
                gameState.breakDownLeft();
                repaint();
                break;

            case KeyEvent.VK_E:
                gameState.breakDownRight();
                repaint();
                break;

            case KeyEvent.VK_F:
                gameState.interact();
                repaint();
                break;

            case KeyEvent.VK_P:
                guardarPartida();
                break;

            case KeyEvent.VK_O:
                carregarPartida();
                break;

            default:
                break;
        }
    }

    /*
     * Executa un torn normal.
     *
     * Important:
     * No feim un while amb temps dins el KeyListener perquè això bloqueja Swing.
     * La caiguda ja hauria d'estar gestionada dins GameState/takeTurn().
     */
    private void playTurn(Direction direction) {
        gameState.takeTurn(direction);
        repaint();
    }

    /*
     * Guarda la partida actual.
     */
    public void guardarPartida() {
        File savesFolder = new File("saves");

        if (!savesFolder.exists()) {
            savesFolder.mkdirs();
        }

        String nomArxiu = JOptionPane.showInputDialog(
                this,
                "Introdueix el nom de la partida, sense extensió. Deixa-ho buit per usar un nom genèric."
        );

        if (nomArxiu == null) {
            return;
        }

        nomArxiu = nomArxiu.trim();

        if (nomArxiu.isEmpty()) {
            String[] saves = savesFolder.list();
            int saveAmount = saves == null ? 0 : saves.length;
            nomArxiu = "partidaGuardada" + saveAmount;
        }

        File saveFile = new File(savesFolder, nomArxiu + ".milm");

        try (ObjectOutputStream file = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            file.writeObject(gameState);
            System.out.println("Partida guardada a: " + saveFile.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No s'ha pogut guardar la partida.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    /*
     * Carrega una partida guardada.
     */
    public void carregarPartida() {
        File savesFolder = new File("saves");

        if (!savesFolder.exists()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No existeix la carpeta saves.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JFileChooser chooser = new JFileChooser(savesFolder);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arxius MILM", "milm");
        chooser.setFileFilter(filter);

        int result = chooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();

        try (ObjectInputStream file = new ObjectInputStream(new FileInputStream(selectedFile))) {
            this.gameState = (GameState) file.readObject();

            resizePanelToGame();
            repaint();

            System.out.println("Partida carregada: " + selectedFile.getAbsolutePath());

        } catch (IOException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No s'ha pogut carregar la partida.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    /*
     * Dibuix principal.
     */
    @Override
    protected void paintComponent(Graphics g) {
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

                switch (tile) {
                    case WALL:
                        drawWall(g, row, col);
                        break;

                    case ICE:
                        drawIce(g, row, col);
                        break;

                    case ICECREAM:
                        drawIceCream(g, row, col);
                        break;

                    case STAIR:
                        drawStair(g, row, col);
                        break;

                    case RAIL:
                        drawRail(g, row, col);
                        break;

                    case DOOR:
                        drawDoor(g, row, col);
                        break;

                    case STONE:
                        drawStone(g, row, col);
                        break;

                    case MOLTEN:
                        drawMolten(g, row, col);
                        break;

                    case BLANK:
                    default:
                        drawBlank(g, row, col);
                        break;
                }
            }
        }
    }

    private void drawBlank(Graphics g, int row, int col) {
        drawCellBackground(g, row, col, new Color(10, 20, 35));
    }

    private void drawWall(Graphics g, int row, int col) {
        drawCellBackground(g, row, col, new Color(70, 70, 80));
        drawEmoji(g, "🧱", row, col, null, font);
    }

    private void drawStone(Graphics g, int row, int col) {
        drawCellBackground(g, row, col, new Color(35, 35, 45));
        drawEmoji(g, "🪨", row, col, null, font);
    }

    private void drawIce(Graphics g, int row, int col) {
        drawCellBackground(g, row, col, new Color(170, 225, 255));
        drawEmoji(g, "🧊", row, col, null, font);
    }

    private void drawIceCream(Graphics g, int row, int col) {
        drawCellBackground(g, row, col, new Color(170, 225, 255));
        drawEmoji(g, "🍦", row, col, null, font);
    }

    private void drawStair(Graphics g, int row, int col) {
        drawCellBackground(g, row, col, new Color(15, 25, 40));
        drawEmoji(g, "🪜", row, col, null, font);
    }

    private void drawRail(Graphics g, int row, int col) {
        drawCellBackground(g, row, col, new Color(15, 25, 40));
        drawEmoji(g, "━", row, col, new Color(134, 0, 179), font);
    }

    private void drawMolten(Graphics g, int row, int col) {
        drawCellBackground(g, row, col, new Color(35, 10, 10));
        drawEmoji(g, "🕳️", row, col, null, font);
    }

    /*
     * Dibuixa una porta només si l'objectiu està completat.
     */
    private void drawDoor(Graphics g, int row, int col) {
        drawCellBackground(g, row, col, new Color(40, 25, 10));

        if (checkObjective()) {
            drawEmoji(g, "🚪", row, col, null, font);
        }
    }

    /*
     * Fons i quadrícula d'una casella.
     */
    private void drawCellBackground(Graphics g, int row, int col, Color color) {
        int x = col * TILE_SIZE;
        int y = row * TILE_SIZE;

        g.setColor(color);
        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);

        g.setColor(new Color(70, 100, 120));
        g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
    }

    /*
     * Dibuixa el jugador.
     */
    private void drawPlayer(Graphics g) {
        Player player = gameState.getPlayer();

        drawEmoji(
                g,
                player.getAvatar(),
                player.getRow(),
                player.getCol(),
                player.getColor(),
                font
        );
    }

    /*
     * Dibuixa els enemics.
     */
    private void drawEnemies(Graphics g) {
        for (Enemy enemy : gameState.getEnemies()) {
            if (!enemy.getIsDead()) {
                drawEmoji(
                        g,
                        enemy.getAvatar(),
                        enemy.getRow(),
                        enemy.getCol(),
                        enemy.getColor(),
                        font
                );
            }
        }
    }

    /*
     * Dibuixa el HUD sota el mapa.
     */
    private void drawHUD(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        int hudY = gameState.getRows() * TILE_SIZE;
        int hudHeight = HUD_HEIGHT;

        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(0, hudY, getWidth(), hudHeight);

        g2.setColor(new Color(70, 100, 120));
        g2.drawLine(0, hudY, getWidth(), hudY);

        Player player = gameState.getPlayer();

        int baseRow = gameState.getRows();
        int baseCol = 0;

        drawEmoji(g, player.getAvatar(), baseRow, baseCol + 1, player.getColor(), font);
        drawEmoji(g, "🍦", baseRow, baseCol + 4, null, font);
        drawText(g, player.geticeCream() + " / " + gameState.getIceCream(), baseRow, baseCol + 5, Color.WHITE);

        drawText(g, "Q: esquerra", baseRow + 1, baseCol + 1, Color.WHITE);
        drawText(g, "E: dreta", baseRow + 1, baseCol + 5, Color.WHITE);
        drawText(g, "P: guardar", baseRow + 1, baseCol + 9, Color.WHITE);
        drawText(g, "O: carregar", baseRow + 1, baseCol + 13, Color.WHITE);
    }

    /*
     * Comprova si es pot mostrar la porta.
     */
    private boolean checkObjective() {
        Player player = gameState.getPlayer();

        return player.geticeCream() >= gameState.getIceCream();
    }

    /*
     * Dibuixa text normal dins una zona tipus casella.
     */
    private void drawText(Graphics g, String text, int row, int col, Color color) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        g2.setFont(font);
        g2.setColor(color);

        int x = col * TILE_SIZE;
        int y = row * TILE_SIZE + 25;

        g2.drawString(text, x, y);
    }

    /*
     * Dibuixa un emoji centrat dins una casella.
     */
    private void drawEmoji(Graphics g, String emoji, int row, int col, Color color, Font f) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        g2.setFont(f);

        if (color != null) {
            g2.setColor(color);
        } else {
            g2.setColor(Color.WHITE);
        }

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
