package gemini.penguinrunner.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import gemini.penguinrunner.assets.AssetManager;
import gemini.penguinrunner.assets.SpriteConfig;
import gemini.penguinrunner.entities.Enemy;
import gemini.penguinrunner.entities.Player;
import gemini.penguinrunner.states.FallingState;
import gemini.penguinrunner.world.Block;
import gemini.penguinrunner.world.Direction;
import gemini.penguinrunner.world.TileType;
import gemini.penguinrunner.world.World;

/**
 * Main game canvas handling double-buffered rendering, tick timers, and user input.
 */
public class GamePanel extends JPanel {
    public static final int TILE_SIZE = 40;

    private final JFrame ownerFrame;
    private final World world;
    private final AssetManager assets;

    private final Timer renderTimer;
    private final Timer fallingTimer;

    public GamePanel(JFrame ownerFrame, World world, AssetManager assets) {
        this.ownerFrame = ownerFrame;
        this.world = world;
        this.assets = assets;

        setBackground(new Color(10, 12, 18));
        setFocusable(true);

        // 60 FPS Repaint timer
        renderTimer = new Timer(16, e -> repaint());
        renderTimer.start();

        // 150ms Gravity tick timer for falling mechanics
        fallingTimer = new Timer(150, e -> {
            if (world.player() != null && world.player().state() instanceof FallingState && !world.isGameOver()) {
                world.movePlayerDownOne();
                world.stepTurn();
            }
        });
        fallingTimer.start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e);
            }
        });
    }

    public void updatePanelSize() {
        int width = world.cols() * TILE_SIZE;
        int height = world.rows() * TILE_SIZE + HudRenderer.HUD_HEIGHT;
        setPreferredSize(new Dimension(width, height));
        if (ownerFrame != null) {
            ownerFrame.pack();
            ownerFrame.setLocationRelativeTo(null);
        }
    }

    private void handleInput(KeyEvent e) {
        if (world.isGameOver()) {
            if (e.getKeyCode() == KeyEvent.VK_R || e.getKeyCode() == KeyEvent.VK_ENTER) {
                world.player().resetLives();
                world.restartLevel();
            }
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> world.handlePlayerInput(Direction.UP);
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> world.handlePlayerInput(Direction.DOWN);
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> world.handlePlayerInput(Direction.LEFT);
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> world.handlePlayerInput(Direction.RIGHT);
            case KeyEvent.VK_SPACE -> world.passTurn();
            case KeyEvent.VK_Q -> world.breakBlock(-1);
            case KeyEvent.VK_E -> world.breakBlock(1);
            case KeyEvent.VK_F -> world.useItem();
            case KeyEvent.VK_Z -> world.player().cycleItem(-1);
            case KeyEvent.VK_X -> world.player().cycleItem(1);
            case KeyEvent.VK_R -> world.restartLevel();
            case KeyEvent.VK_ESCAPE -> openPauseMenu();
        }
    }

    private void openPauseMenu() {
        PauseMenuDialog dialog = new PauseMenuDialog(
            ownerFrame,
            assets.audio(),
            () -> saveGame(),
            () -> loadGame()
        );
        dialog.setVisible(true);
    }

    private void saveGame() {
        File savesDir = new File("saves");
        if (!savesDir.exists()) savesDir.mkdirs();
        File saveFile = new File(savesDir, "save.json");
        gemini.penguinrunner.io.SaveManager.save(world, saveFile);
    }

    private void loadGame() {
        File saveFile = new File("saves", "save.json");
        if (saveFile.exists()) {
            gemini.penguinrunner.io.SaveManager.load(world, saveFile);
            updatePanelSize();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        int rows = world.rows();
        int cols = world.cols();

        // 1. Draw World Grid Tiles
        Block[][] blocks = world.blocks();
        if (blocks != null) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    Block block = blocks[r][c];
                    if (block == null) continue;
                    drawBlock(g2, block, r, c);
                }
            }
        }

        // 2. Draw Enemies
        for (Enemy enemy : world.enemies()) {
            if (!enemy.isDead()) {
                drawEntity(g2, enemy.spriteKey(), enemy.row(), enemy.col());
            }
        }

        // 3. Draw Player
        Player player = world.player();
        if (player != null && !player.isDead()) {
            drawEntity(g2, "player", player.row(), player.col());
        }

        // 4. Draw HUD
        HudRenderer.render(g2, world, assets, getWidth(), rows * TILE_SIZE, TILE_SIZE);

        // 5. Game Over Overlay
        if (world.isGameOver()) {
            drawGameOverOverlay(g2);
        }
    }

    private void drawBlock(Graphics2D g2, Block block, int r, int c) {
        TileType type = block.type();
        if (type == TileType.BLANK || type == TileType.EMPTY) return;

        int x = c * TILE_SIZE;
        int y = r * TILE_SIZE;

        String key = switch (type) {
            case ICE -> (block.autotileKey() != null) ? block.autotileKey() : "ice";
            case WALL -> "wall";
            case STAIR -> "stair";
            case RAIL -> "rail";
            case STONE -> "stone";
            case WOOD -> "wood";
            case DOOR -> block.isDoorOpen() ? "door" : null;
            case TRAPDOOR -> "trapdoor";
            case BUTTON -> "button";
            case ICECREAM -> "icecream";
            case FLAMETHROWER -> "flamethrower";
            case TELEPORT -> "teleport";
            case MOLTEN -> "molten";
            case BURNT -> "burnt";
            default -> null;
        };

        if (key == null) return;
        drawSpriteOrFallback(g2, key, x, y, TILE_SIZE, TILE_SIZE);
    }

    private void drawEntity(Graphics2D g2, String spriteKey, int r, int c) {
        int x = c * TILE_SIZE;
        int y = r * TILE_SIZE;
        drawSpriteOrFallback(g2, spriteKey, x, y, TILE_SIZE, TILE_SIZE);
    }

    private void drawSpriteOrFallback(Graphics2D g2, String key, int x, int y, int w, int h) {
        SpriteConfig cfg = assets.getSprite(key);
        if (cfg != null && cfg.image() != null) {
            g2.drawImage(cfg.image(), x, y, w, h, null);
        } else if (cfg != null) {
            // Emoji / Text fallback
            g2.setFont(assets.emojiFont());
            g2.setColor(cfg.color());
            FontMetrics fm = g2.getFontMetrics();
            int strX = x + (w - fm.stringWidth(cfg.emoji())) / 2;
            int strY = y + (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(cfg.emoji(), strX, strY);
        }
    }

    private void drawGameOverOverlay(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, w, h);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
        g2.setColor(new Color(255, 80, 80));
        FontMetrics fm = g2.getFontMetrics();
        String msg = "GAME OVER";
        g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2 - 20);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);
        FontMetrics fm2 = g2.getFontMetrics();
        String sub = "Press 'R' or 'Enter' to restart";
        g2.drawString(sub, (w - fm2.stringWidth(sub)) / 2, h / 2 + 25);
    }
}

