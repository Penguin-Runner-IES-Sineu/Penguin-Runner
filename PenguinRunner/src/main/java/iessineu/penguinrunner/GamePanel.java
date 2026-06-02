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
import java.awt.GraphicsEnvironment;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import iessineu.penguinrunner.Blocks.Block;
import iessineu.penguinrunner.Blocks.TileType;
import iessineu.penguinrunner.Entity.Enemy;
import iessineu.penguinrunner.Entity.Player;
import iessineu.penguinrunner.Movement.Direction;

public class GamePanel extends JPanel implements Serializable {

    public static final int TILE_SIZE = 43;
    private static final int HUD_HEIGHT = 100;
    // private static String printablesPath = "resources/printables_webdings.json";
    // private String emojiFontPath = "resources/WEBDINGS.ttf";
    // private String emojiFontPath = "resources/google.ttf";
    // private static String printablesPath = "resources/printables_google.json";
    // private String textFontPath = "resources/sonic.ttf";
    // private String emojiFontPath = textFontPath;
    // private static String printablesPath = "resources/printables.json";
    // private String emojiFontPath = "resources/font.ttf";
    // private String textFontPath = emojiFontPath;
    // private String textFontPath = "resources/wings.ttf";
    private static String folderPath = "resources/";
    private static String musicPath = folderPath + "music.wav";
    private static String printablesPath = folderPath + "printables.json";
    private static String emojiFontPath = folderPath + "emoji.ttf";
    private static String textFontPath = folderPath + "font.ttf";
    private static Map<String, List<String>> spriteMap = createSpriteMap();

    private Font textFont;
    private Font emojiFont;
    private final SoundManager soundManager = new SoundManager();
    private final GameFrame gameFrame;
    private GameState gameState;
    

    // private InputMap inputMap = getInputMap();
    // private ActionMap actionMap = getActionMap();
    public GamePanel(GameFrame frame) {
        gameFrame = frame;
        gameState = new GameState();
        soundManager.playMusic(musicPath);
        soundManager.setVolume(0.7f);
        loadFonts();
        Printable.setFont(emojiFont);
        resizePanelToGame();

        setBackground(Color.BLACK);

        // Necessari perquè el JPanel pugui rebre tecles.
        setFocusable(true);

        Timer timer = new Timer(20, e -> repaint());
        timer.start();

        // Escoltar teclat.
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e);
            }
        });
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
        // gameState.takeTurn(direction);
    }

    /*
     * Carrega la font externa. Si falla, usa una font del sistema.
     */
    private void loadFonts() {
        emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 30); // per defecte s'empra aquesta, i després llegim l'arxiu 
        textFont = new Font("Segoe UI Emoji", Font.PLAIN, 30); // per defecte s'empra aquesta, i després llegim l'arxiu 
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            // ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(emojiFontPath)));
            // ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(textFontPath)));
            // emojiFont = Font.createFont(Font.TRUETYPE_FONT, new File(emojiFontPath)).deriveFont(30f);
            // textFont = Font.createFont(Font.TRUETYPE_FONT, new File(textFontPath)).deriveFont(30f);
            emojiFont = Font.createFont(Font.TRUETYPE_FONT, new File(emojiFontPath)).deriveFont(30f);
            textFont = Font.createFont(Font.TRUETYPE_FONT, new File(textFontPath)).deriveFont(30f);
            ge.registerFont(emojiFont);
            ge.registerFont(textFont);
        } catch (FontFormatException | IOException ex) {
            System.out.println("Error obrint alguna de les font!");
            System.getLogger(GamePanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    /*
     * Ajusta la mida del panell al mapa actual.
     * És útil també després de carregar una partida.
     */
    private void resizePanelToGame() {
        int width = gameState.getCols() * TILE_SIZE;
        int height = gameState.getRows() * TILE_SIZE + HUD_HEIGHT;
        setPreferredSize(new Dimension(width, height));
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null);

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
            case KeyEvent.VK_Q -> {
                gameState.breakDownLeft();
            }
            case KeyEvent.VK_E -> {
                gameState.breakDownRight();
            }
            case KeyEvent.VK_F -> {
                gameState.interact();
                resizePanelToGame();
            }
            case KeyEvent.VK_P ->
                guardarPartida();
            case KeyEvent.VK_O ->
                carregarPartida();
        }
        playTurn(direction);
        if (direction == null) {
            gameState.updateLogic();
        }
        // repaint();
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

            // repaint();
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
        gameState.reloadSprites();
        resizePanelToGame();
        // repaint();
    }

    /*
     * Dibuix principal.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Printable.setGraphics(g);
        drawMap();
        drawEnemies();
        drawPlayer();
        drawHUD(g);
    }

    /*
     * Dibuixa el mapa casella per casella.
     */
    private void drawMap() {
        for (int row = 0; row < gameState.getRows(); row++) {
            for (int col = 0; col < gameState.getCols(); col++) {
                Block b = gameState.getBlocks()[row][col];
                if (b.isDoor()) {
                    if (gameState.checkObjective()) {
                        b.setType(TileType.DOOR);
                        b.setPrintables();
                    }
                }
                b.draw(row, col);
            }
        }
    }

    /*
     * Dibuixa el jugador.
     */
    private void drawPlayer() {
        Player player = gameState.getPlayer();
        player.draw(player.getRow(), player.getCol());
    }

    /*
     * Dibuixa els enemics.
     */
    private void drawEnemies() {
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
        Block icecream = new Block(TileType.ICECREAM);

        int padding = 20;
        int textY = hudY + 35;

        g2.setFont(textFont.deriveFont(16f));
        g2.setColor(Color.WHITE);

        player.draw(gameState.getRows(), 1);
        icecream.draw(gameState.getRows(), 5);
        g2.drawString(player.geticeCream() + "/ " + gameState.getIceCream(), 250, textY);

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

    public static Map<String, List<String>> createSpriteMap() {
        String jsonString = "";
        try {
            BufferedReader fitxer = new BufferedReader(new FileReader(GamePanel.printablesPath));
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
                File fileSprite = new File(folderPath + "sprites/" + fileString);
                if (!fileSprite.exists()) {
                    System.out.println("El fitxer de sprite per " + obj.toString() + " no s'ha trobat");
                }
                atributs.add(fileString);
            } catch (JSONException e) {
                System.out.println("No s'ha trobat Arxiu per l'element" + obj.toString());
            }
            spriteMap.put(type, atributs);
        }
        return spriteMap;
    }

    public GameState getGameState() {
        return gameState;
    }

    public static String getPrintablesPath() {
        return printablesPath;
    }

    public static void setPrintablesPath(String printablesPath) {
        GamePanel.printablesPath = printablesPath;
    }

    public static String getEmojiFontPath() {
        return emojiFontPath;
    }

    public static void setEmojiFontPath(String emojiFontPath) {
        GamePanel.emojiFontPath = emojiFontPath;
    }

    public static String getTextFontPath() {
        return textFontPath;
    }

    public static void setTextFontPath(String textFontPath) {
        GamePanel.textFontPath = textFontPath;
    }

    public static String getMusicPath() {
        return musicPath;
    }

    public static void setMusicPath(String musicPath) {
        GamePanel.musicPath = musicPath;
    }

    public static String getFolderPath() {
        return folderPath;
    }

    public static void setFolderPath(String folderPath) {
        GamePanel.folderPath = folderPath;
    }

    public static Map<String, List<String>> getSpriteMap() {
        return spriteMap;
    }

    public static void setSpriteMap(Map<String, List<String>> spriteMap) {
        GamePanel.spriteMap = spriteMap;
    }

    public static void updatePaths() {
        musicPath = folderPath + "music.wav";
        printablesPath = folderPath + "printables.json";
        emojiFontPath = folderPath + "font.ttf";
        textFontPath = folderPath + "font.ttf";
        System.out.println("S'han actualitzat les rutes.");
        System.out.println("Rutes actuals:");
        System.out.println("Ruta de musica:" + musicPath);
        System.out.println("Ruta de printables:" + printablesPath);
        System.out.println("Ruta de font (emoji)" + emojiFontPath);
        System.out.println("Ruta de font (text)" + textFontPath);
    }

}
