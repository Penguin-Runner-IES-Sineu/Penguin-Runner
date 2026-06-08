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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import iessineu.penguinrunner.Blocks.Block;
import iessineu.penguinrunner.Blocks.TileType;
import iessineu.penguinrunner.Entity.Items.Flamethrower;
import iessineu.penguinrunner.Entity.Items.Item;
import iessineu.penguinrunner.Entity.Player;
import iessineu.penguinrunner.Entity.enemies.Enemy;
import iessineu.penguinrunner.Movement.Direction;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;

public class GamePanel extends JPanel implements Serializable {

    private static boolean game = false;
    public static final int TILE_SIZE = 43;
    private static final int HUD_HEIGHT = 100;
    private static String folderPath = "resources/";
    private static String musicPath = folderPath + "music.wav";
    private static String printablesPath = folderPath + "printables.json";
    private static String emojiFontPath = folderPath + "emoji.ttf";
    private static String textFontPath = folderPath + "font.ttf";
    private static Map<String, List<String>> spriteMap = createSpriteMap(!GamePanel.hasGame());
    private Font textFont;
    private Font emojiFont;
    private final GameFrame gameFrame;
    private GameState gameState;
    public Timer timer = new Timer(20, e -> repaint());
    public Timer deathTimer;

    public GamePanel(GameFrame frame) {
        gameFrame = frame;
        gameState = new GameState();
        gameState.playMusic(!GamePanel.hasGame());
        loadFonts(!GamePanel.hasGame());
        Printable.setFont(emojiFont);
        resizePanelToGame();

        setBackground(Color.BLACK);

        // Necessari perquè el JPanel pugui rebre tecles.
        setFocusable(true);

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
    private void loadFonts(boolean fromResource) {
        emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 30); // per defecte s'empra aquesta, i després llegim l'arxiu 
        textFont = new Font("Segoe UI Emoji", Font.PLAIN, 16); // per defecte s'empra aquesta, i després llegim l'arxiu
        ClassLoader classLoader = PenguinRunner.class.getClassLoader();
        InputStream recTextFont = classLoader.getResourceAsStream("font.ttf");
        InputStream recEmojiFont = classLoader.getResourceAsStream("emoji.ttf");
        System.out.print("Caregant fonts: ");
        try {
            if (fromResource) {
                System.out.println("Carregant Recurs");
                emojiFont = Font.createFont(Font.TRUETYPE_FONT, recEmojiFont).deriveFont(30f);
                textFont = Font.createFont(Font.TRUETYPE_FONT, recTextFont).deriveFont(16f);
            } else {
                System.out.println("Carregant Fitxer");
                emojiFont = Font.createFont(Font.TRUETYPE_FONT, new File(emojiFontPath)).deriveFont(30f);
                textFont = Font.createFont(Font.TRUETYPE_FONT, new File(textFontPath)).deriveFont(16f);
            }
        } catch (FontFormatException | IOException ex) {
            System.out.println("Error obrint alguna de les font!");
            System.getLogger(GamePanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    private void loadModdedFont(String type) {
        try {
            if (type.equals("emoji")) {
                emojiFont = Font.createFont(Font.TRUETYPE_FONT, new File(emojiFontPath)).deriveFont(30f);
            } else {
                textFont = Font.createFont(Font.TRUETYPE_FONT, new File(textFontPath)).deriveFont(16f);
            }
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
            case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                direction = Direction.UP;
                playTurn(direction);
            }
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                direction = Direction.DOWN;
                playTurn(direction);
            }
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                direction = Direction.LEFT;
                playTurn(direction);
            }
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                direction = Direction.RIGHT;
                playTurn(direction);
            }
            case KeyEvent.VK_SPACE -> {
                gameState.updateLogic();
            }
            case KeyEvent.VK_Q -> {
                gameState.breakDownLeft();
            }
            case KeyEvent.VK_E -> {
                gameState.breakDownRight();
            }
            case KeyEvent.VK_F -> {
                gameState.useItem();
            }
            case KeyEvent.VK_X -> {
                gameState.changeItem(false);
            }
            case KeyEvent.VK_Z -> {
                gameState.changeItem(true);
            }
            case KeyEvent.VK_R -> {
                gameState.playerDied(true);
            }
            case KeyEvent.VK_ESCAPE ->
                menuPausa();
            default -> {
            }
        }
        if (!gameState.isGameOver()) {
            gameFrame.setVisible(true);
            if (gameState.isMuted()) {
                gameState.unmuteMusic();
            }
            gameState.interact();
            resizePanelToGame();
        } else {
            gameFrame.setVisible(false);
            gameState.muteMusic();
            loadDeathMenu();
        }
    }

    public void loadDeathMenu() {
        MenuFrame deathMenu = new MenuFrame(true);
        deathMenu.setVisible(true);
        deathTimer = new Timer(200, e -> reloadGame(deathMenu));
        deathTimer.start();
    }

    public void reloadGame(MenuFrame menu) {
        boolean arrancar = menu.arrancar();
        if (arrancar) {
            deathTimer.stop();
            menu.setVisible(false);
            gameFrame.setVisible(true);
            gameState.setGameOver(false);
            gameState.unmuteMusic();
            resizePanelToGame();
        }
    }

    public void menuPausa() {
        PauseMenuDialog pauseMenu = new PauseMenuDialog(gameFrame, gameState.getGameVolume());
        pauseMenu.setVisible(true);

        /*
     * Quan es tanca el menú, aplicam el volum seleccionat.
         */
        gameState.changeVolume(pauseMenu.getVolume());

        switch (pauseMenu.getSelectedOption()) {
            case PauseMenuDialog.RESUME -> {
                gameState.unmuteMusic();
                requestFocusInWindow();
            }

            case PauseMenuDialog.SAVE -> {
                gameState.unmuteMusic();
                guardarPartida();
                requestFocusInWindow();
            }

            case PauseMenuDialog.LOAD -> {
                gameState.unmuteMusic();
                carregarPartida();
                requestFocusInWindow();
            }

            case PauseMenuDialog.EXIT -> {
                System.exit(0);
            }

            default -> {
                gameState.unmuteMusic();
                requestFocusInWindow();
            }
        }
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
                    "No s'ha pogut guardar la partida. Consulta la consola per més informació",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
        JOptionPane.showMessageDialog(
                this,
                "S'ha guardat la partida."
        );
//        menuPausa();
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
        String path = selectedFile.toString();
        if (!path.endsWith(".milm")) {
            selectedFile = new File(path + ".milm");
        }

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
        gameState.reloadAudioAfterLoad();
        resizePanelToGame();
        requestFocusInWindow();
        repaint();
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
        IceAutoTileConnector.update(gameState.getBlocks());

        for (int row = 0; row < gameState.getRows(); row++) {
            for (int col = 0; col < gameState.getCols(); col++) {
                Block b = gameState.getBlocks()[row][col];
                if (b != null) {
                    if (b.isDoor()) {
                        if (gameState.checkObjective()) {
                            b.setType(TileType.DOOR);
                            b.setPrintables();
                        }
                    }
                    if (b.isTrapdoor()) {
                        if (gameState.buttonPressed()) {
                            b.setType(TileType.TRAPDOOR);
                            b.setSolid(true);
                        } else {
                            b.setType(TileType.BLANK);
                            b.setSolid(false);
                        }
                        b.setPrintables();
                    }
                    b.draw(row, col);
                }
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
        Font originalEmojiFont = emojiFont;
        Printable.setFont(textFont.deriveFont(16f));

        int hudY = gameState.getRows() * TILE_SIZE;
        int hudHeight = HUD_HEIGHT;

        // Fons del HUD
        g2.setColor(new Color(18, 18, 28));
        g2.fillRect(0, hudY, getWidth(), hudHeight);

        // Línia superior
        g2.setColor(new Color(90, 130, 160));
        g2.drawLine(0, hudY, getWidth(), hudY);

        // int padding = 20;
        // int textY = hudY + 35;
        // g2.setFont(textFont.deriveFont(16f));
        // g2.setColor(Color.WHITE);
        // g2.drawString(player.geticeCream() + "/ " + gameState.getIceCream(), 250, textY);
        // g2.drawString(
        //         "Nivell: " + (gameState.getNivell() + 1),
        //         padding + 430,
        //         textY
        // );
        // g2.setColor(new Color(190, 210, 230));
        // g2.drawString(
        //         "←↑→↓/WASD: moure   Q/E: trencar   F: interactuar   P: guardar   O: carregar",
        //         padding,
        //         textY + 35
        // );
        Player player = gameState.getPlayer();
        Block icecream = new Block(TileType.ICECREAM);

        for (int i = 0; i < player.getLives(); i++) {
            player.draw(gameState.getRows(), 1 + i);
        }

        icecream.draw(gameState.getRows(), 5);

        Block vides = new Block(TileType.BLANK);
        vides.setEmoji(player.geticeCream() + "/ " + gameState.getIceCream());
        vides.setColorFromHex("#FFFFFF");
        vides.draw(gameState.getRows(), 6);

        Block nivell = new Block(TileType.BLANK);
        nivell.setEmoji("Nivell: " + (gameState.getNivell() + 1));
        nivell.setColorFromHex("#FFFFFF");
        nivell.draw(gameState.getRows(), 8);

        Block controls = new Block(TileType.BLANK);
        Printable.setFont(textFont.deriveFont(11f));
        controls.setEmoji("←↑→↓/WASD: moure, Q/E Rompre blocs, Z/X Canviar objecte, F: Interactuar, R: Reiniciar nivell ESC: Pausa");
        controls.setColorFromHex("#FFFFFF");
        controls.draw(gameState.getRows() + 1, (gameState.getCols() / 2) - 2);
        Printable.setFont(textFont.deriveFont(16f));

        if (player.hasItems()) {
            List<Item> items = player.getItems();
            int originalPointer = player.getSelectedItemIndex();
            for (int i = 0; i < 3; i++) {
                player.nextItem();
                int pointer = player.getSelectedItemIndex();
                Block item = null;
                switch (items.get(pointer).getName().toLowerCase()) {
                    case "flamethrower" -> {
                        item = new Block(TileType.FLAMETHROWER);
                        Block usos = new Block(TileType.BLANK);
                        Flamethrower f = (Flamethrower) player.getSelectedItem();
                        usos.setEmoji("(" + f.getUsesLeft() + ")");
                        usos.setColorFromHex("#FFFFFF");
                        usos.draw(gameState.getRows() + 1, gameState.getCols() - 3 - i);
                    }
                    case "teleport" -> {
                        item = new Block(TileType.TELEPORT);
                    }
                }
                if (item != null) {
                    item.draw(gameState.getRows(), gameState.getCols() - 3 - i);
                }
            }
            player.setSelectedItemIndex(originalPointer);
            Block arrow = new Block(TileType.EMPTY);
            ClassLoader classLoader = PenguinRunner.class.getClassLoader();
            InputStream is = classLoader.getResourceAsStream("sprites/selected.png");
            ImageIcon icon = null;
            try {
                icon = new ImageIcon(ImageIO.read(is));
            } catch (IOException ex) {
                System.getLogger(GamePanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            arrow.hardCodeSprite(icon);
            arrow.draw(gameState.getRows(), gameState.getCols() - 4);
        }

        Printable.setFont(originalEmojiFont);
    }

    public static Map<String, List<String>> createSpriteMap(boolean fromResource) {
        String jsonString = "";
        BufferedReader fitxer;
        ClassLoader classLoader = PenguinRunner.class.getClassLoader();
        InputStream is = classLoader.getResourceAsStream("printables.json");
        System.out.print("Caregant Printables: ");
        try {
            if (fromResource) {
                System.out.println("Carregant Recurs");
                fitxer = new BufferedReader(new InputStreamReader(is));
                if (is == null) {
                    throw new IOException("Resource not found: maps.json");
                }
            } else {
                System.out.println("Carregant Fitxer");
                fitxer = new BufferedReader(new FileReader(printablesPath));
            }
            try {
                String line;

                while ((line = fitxer.readLine()) != null) {
                    jsonString += line;
                }

                fitxer.close();

            } catch (IOException ex) {
                System.out.println("Problema d'entrada i sortida");
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                if (hasGame()) {
                    File fileSprite = new File(folderPath + "sprites/" + fileString);
                    if (!fileSprite.exists()) {
                        System.out.println("El fitxer de sprite per " + obj.toString() + " no s'ha trobat");
                    }
                }
                atributs.add(fileString);
            } catch (JSONException e) {
                System.out.println("No s'ha trobat Arxiu per l'element" + obj.toString());
            }
            spriteMap.put(type, atributs);
        }
        return spriteMap;
    }

    public void loadMods(String path) {
        String jsonString = "";
        System.out.print("Carregant Mod: ");
        BufferedReader fitxer = null;
        System.out.println(path + "/mods.json");
        try {
            fitxer = new BufferedReader(new FileReader(path + "/mods.json"));
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
            System.getLogger(GamePanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        JSONArray modsArray = new JSONArray(jsonString);
        for (int i = 0; i < modsArray.length(); i++) {
            JSONObject mod = modsArray.getJSONObject(i);
            String modType = mod.getString("modtype");
            JSONObject modValue = mod.getJSONObject("modvalue");
            gameState.addMod(modType, modValue, path);
            if (modType.equals("font")) {
                String originalFilename = modValue.getString("filename");
                modValue.put("filename", path + "/" + originalFilename);
                if (modValue.getString("type").equals("emoji")) {
                    setEmojiFontPath(modValue.getString("filename"));
                } else {
                    setTextFontPath(modValue.getString("filename"));
                }
                loadModdedFont(modType);
            }
        }
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

    public static boolean hasGame() {
        return game;
    }

    public static void setGame(boolean game) {
        GamePanel.game = game;
    }

    public static void updatePaths() {
        musicPath = folderPath + "music.wav";
        printablesPath = folderPath + "printables.json";
        emojiFontPath = folderPath + "emoji.ttf";
        textFontPath = folderPath + "font.ttf";
        System.out.println("S'han actualitzat les rutes.");
        System.out.println("Rutes actuals:");
        System.out.println("Ruta de musica:" + musicPath);
        System.out.println("Ruta de printables:" + printablesPath);
        System.out.println("Ruta de font (emoji)" + emojiFontPath);
        System.out.println("Ruta de font (text)" + textFontPath);
    }

}
