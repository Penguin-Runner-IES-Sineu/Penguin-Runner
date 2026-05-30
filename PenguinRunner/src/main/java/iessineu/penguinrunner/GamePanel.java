/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import iessineu.penguinrunner.Blocks.Block;
import iessineu.penguinrunner.Blocks.TileType;
import iessineu.penguinrunner.Entity.Enemy;
import iessineu.penguinrunner.Entity.Player;
import iessineu.penguinrunner.Movement.Direction;

public class GamePanel extends JPanel {

    public static final int TILE_SIZE = 43;
    private static final int HUD_HEIGHT = 100;
    private static String printablesPath = "resources/printables.json";
    private Image blankSprite;
    private Font font;
    private final SoundManager soundManager = new SoundManager();
    private GameState gameState = new GameState();

    Block mapa[][] = gameState.loadMap();

    public GamePanel() {
        soundManager.playMusic("resources/music.wav");
        soundManager.setVolume(0.7f);
        // gameState = new GameState();
        loadFont();
        createSpriteMap();
        Printable.setFont(font);
        mapa = gameState.loadMap();

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
     * Carrega la font externa. Si falla, usa una font del sistema.
     */
    private Font loadFont() {
        font = new Font("Segoe UI Emoji", Font.PLAIN, 30); // per defecte s'empra aquesta, i després llegim l'arxiu 
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
            case KeyEvent.VK_UP ->
                playTurn(Direction.UP);
            case KeyEvent.VK_DOWN ->
                playTurn(Direction.DOWN);
            case KeyEvent.VK_LEFT ->
                playTurn(Direction.LEFT);
            case KeyEvent.VK_RIGHT ->
                playTurn(Direction.RIGHT);
            case KeyEvent.VK_SPACE ->
                playTurn(null);
            case KeyEvent.VK_Q -> {
                gameState.breakDownLeft();
                repaint();
            }
            case KeyEvent.VK_E -> {
                gameState.breakDownRight();
                repaint();
            }
            case KeyEvent.VK_F -> {
                gameState.interact();
                repaint();
            }
            case KeyEvent.VK_P ->
                guardarPartida();
            case KeyEvent.VK_O ->
                carregarPartida();
            default -> {
            }
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

        Printable.setGraphics(g);
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
                if (tile == TileType.BLANK) {
                    drawBlank(g, row, col);
                } else {
                    Block b = gameState.getBlocks()[row][col];
                    b.draw(row, col);
                }
            }
            System.out.println("");
        }
    }

    private void drawBlank(Graphics g, int row, int col) {
        drawSprite(g, blankSprite, row, col);
    }

    /*
     * Dibuixa el jugador.
     */
    private void drawPlayer(Graphics g) {
        Player player = gameState.getPlayer();
        player.draw(player.getRow(), player.getCol());

        // drawSprite(g, playerSprite, player.getRow(), player.getCol());
    }

    /*
     * Dibuixa els enemics.
     */
    private void drawEnemies(Graphics g) {
        for (Enemy enemy : gameState.getEnemies()) {
            if (!enemy.isDead()) {
                enemy.draw(enemy.getRow(), enemy.getCol());
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

        // Fons del HUD
        g2.setColor(new Color(18, 18, 28));
        g2.fillRect(0, hudY, getWidth(), hudHeight);

        // Línia superior
        g2.setColor(new Color(90, 130, 160));
        g2.drawLine(0, hudY, getWidth(), hudY);

        Player player = gameState.getPlayer();

        int padding = 20;
        int textY = hudY + 35;

        g2.setFont(font.deriveFont(16f));
        g2.setColor(Color.WHITE);

        g2.drawString("🐧", padding, textY);

        g2.drawString(
                "🍦 " + player.geticeCream() + " / " + gameState.getIceCream(),
                padding + 220,
                textY
        );

        g2.drawString(
                "Nivell: " + gameState.getNivell(),
                padding + 430,
                textY
        );

        g2.setColor(new Color(190, 210, 230));
        g2.drawString(
                "←↑→↓: moure   Q/E: trencar   F: interactuar   P: guardar   O: carregar",
                padding,
                textY + 35
        );
    }

    /*
     * Comprova si es pot mostrar la porta.
     */
    private boolean checkObjective() {
        Player player = gameState.getPlayer();

        return player.geticeCream() >= gameState.getIceCream();
    }

    public static Map<String, List<String>> createSpriteMap() {
        String jsonString = "";
        try {
            BufferedReader fitxer = new BufferedReader(new FileReader(printablesPath));
            try {
                String line;

                while ((line = fitxer.readLine()) != null) {
                    jsonString += line;
                }

                fitxer.close();

            } catch (IOException ex) {
                System.out.println("Problema d'entrada i sortida");
            }

        } catch (FileNotFoundException ex) {
            System.out.println("L'arxiu no s'ha trobat!");
        }
        Map<String, List<String>> spriteMap = new HashMap();
        JSONArray entities = new JSONArray(jsonString);
        for (int i = 0; i < entities.length(); i++) {
            JSONObject obj = entities.getJSONObject(i);
            String type = "";
            List<String> atributs = new ArrayList();
            try {
                type = obj.getString("type");
            } catch (JSONException e) {
                System.out.println("No s'ha trobat el tipus per l'element " + obj.toString());
            }
            try {
                String emoji = obj.getString("sprite");
                atributs.add(emoji);
            } catch (JSONException e) {
                System.out.println("No s'ha trobat Emoji per l'element " + obj.toString());
            }
            try {
                String colorString = obj.getString("color");
                atributs.add(colorString);
            } catch (JSONException e) {
                System.out.println("No s'ha trobat Color per l'element " + obj.toString());
            }
            try {
                String fileString = obj.getString("filename");
                atributs.add(fileString);
            } catch (JSONException e) {
                System.out.println("No s'ha trobat Arxiu per l'element" + obj.toString());
            }
            spriteMap.put(type, atributs);
        }
        return spriteMap;
    }

    private void drawSprite(Graphics g, Image image, int row, int col) {
        int x = col * TILE_SIZE;
        int y = row * TILE_SIZE;

        g.drawImage(image, x, y, TILE_SIZE, TILE_SIZE, null);
    }

}
