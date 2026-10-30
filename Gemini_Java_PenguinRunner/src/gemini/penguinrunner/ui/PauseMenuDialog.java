package gemini.penguinrunner.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JDialog;
import javax.swing.JPanel;

import gemini.penguinrunner.assets.AudioPlayer;

/**
 * Centered, dynamically scaling pause menu modal dialog.
 * Adapts to any level resolution without clipping menu options.
 */
public class PauseMenuDialog extends JDialog {
    public static final int RESUME = 0;
    public static final int VOLUME = 1;
    public static final int SAVE   = 2;
    public static final int LOAD   = 3;
    public static final int QUIT   = 4;

    private int selectedOption = RESUME;
    private final AudioPlayer audio;
    private final Runnable onSave;
    private final Runnable onLoad;

    public PauseMenuDialog(Window owner, AudioPlayer audio, Runnable onSave, Runnable onLoad) {
        super(owner, "Pause", ModalityType.APPLICATION_MODAL);
        this.audio = audio;
        this.onSave = onSave;
        this.onLoad = onLoad;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        PausePanel panel = new PausePanel();
        add(panel);

        int ownerW = Math.max(480, owner.getWidth());
        int ownerH = Math.max(360, owner.getHeight());
        setSize(ownerW, ownerH);
        setLocationRelativeTo(owner);
    }

    private class PausePanel extends JPanel {
        private int selectedIndex = 0;
        private boolean draggingVolume = false;

        private final String[] options = {
            "Continue", "Volume", "Save Game", "Load Game", "Quit"
        };

        private final Color[] colors = {
            new Color(80, 220, 255),
            new Color(170, 230, 255),
            new Color(160, 235, 180),
            new Color(255, 215, 120),
            new Color(255, 130, 130)
        };

        PausePanel() {
            setOpaque(false);
            setFocusable(true);

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                            selectedIndex = (selectedIndex - 1 + options.length) % options.length;
                            repaint();
                        }
                        case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                            selectedIndex = (selectedIndex + 1) % options.length;
                            repaint();
                        }
                        case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                            if (selectedIndex == VOLUME) {
                                audio.setVolume(audio.getVolume() - 0.05f);
                                repaint();
                            }
                        }
                        case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                            if (selectedIndex == VOLUME) {
                                audio.setVolume(audio.getVolume() + 0.05f);
                                repaint();
                            }
                        }
                        case KeyEvent.VK_ENTER, KeyEvent.VK_SPACE -> executeSelected();
                        case KeyEvent.VK_ESCAPE -> dispose();
                    }
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int w = getWidth();
                    int h = getHeight();
                    int placaW = Math.min(360, w - 40);
                    int placaX = (w - placaW) / 2;
                    int startY = h / 4;
                    int spacing = Math.min(50, (h / 2) / options.length);

                    for (int i = 0; i < options.length; i++) {
                        int itemY = startY + i * spacing;
                        Rectangle rect = new Rectangle(placaX + 20, itemY, placaW - 40, spacing - 6);
                        if (rect.contains(e.getPoint())) {
                            selectedIndex = i;
                            if (i == VOLUME) {
                                draggingVolume = true;
                                updateVolumeFromMouse(e.getX(), placaX + 120, placaW - 160);
                            } else {
                                executeSelected();
                            }
                            repaint();
                            return;
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    draggingVolume = false;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (draggingVolume) {
                        int w = getWidth();
                        int placaW = Math.min(360, w - 40);
                        int placaX = (w - placaW) / 2;
                        updateVolumeFromMouse(e.getX(), placaX + 120, placaW - 160);
                        repaint();
                    }
                }
            });
        }

        private void updateVolumeFromMouse(int mouseX, int sliderX, int sliderW) {
            float ratio = (float) (mouseX - sliderX) / (float) sliderW;
            audio.setVolume(Math.max(0f, Math.min(1f, ratio)));
        }

        private void executeSelected() {
            switch (selectedIndex) {
                case RESUME -> dispose();
                case VOLUME -> {} // handled via slider
                case SAVE -> {
                    if (onSave != null) onSave.run();
                    dispose();
                }
                case LOAD -> {
                    if (onLoad != null) onLoad.run();
                    dispose();
                }
                case QUIT -> System.exit(0);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Dim backdrop
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, w, h);

            int placaW = Math.min(360, w - 40);
            int placaH = Math.min(320, h - 40);
            int placaX = (w - placaW) / 2;
            int placaY = (h - placaH) / 2;

            // Plaque background
            g2.setColor(new Color(20, 26, 38, 230));
            g2.fillRoundRect(placaX, placaY, placaW, placaH, 16, 16);
            g2.setColor(new Color(80, 140, 200));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(placaX, placaY, placaW, placaH, 16, 16);

            // Title
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            String title = "PAUSED";
            g2.drawString(title, (w - fm.stringWidth(title)) / 2, placaY + 36);

            // Menu Items
            int startY = placaY + 60;
            int spacing = Math.min(48, (placaH - 80) / options.length);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));

            for (int i = 0; i < options.length; i++) {
                int itemY = startY + i * spacing;
                boolean selected = (i == selectedIndex);

                if (selected) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(placaX + 16, itemY - 20, placaW - 32, spacing - 4, 8, 8);
                    g2.setColor(colors[i]);
                } else {
                    g2.setColor(new Color(200, 210, 220));
                }

                g2.drawString(options[i], placaX + 32, itemY);

                if (i == VOLUME) {
                    // Draw volume slider bar
                    int sliderX = placaX + 130;
                    int sliderY = itemY - 12;
                    int sliderW = placaW - 170;
                    int sliderH = 10;

                    g2.setColor(new Color(60, 70, 90));
                    g2.fillRoundRect(sliderX, sliderY, sliderW, sliderH, 6, 6);

                    int filledW = (int) (sliderW * audio.getVolume());
                    g2.setColor(new Color(80, 220, 255));
                    g2.fillRoundRect(sliderX, sliderY, filledW, sliderH, 6, 6);

                    // Knob
                    g2.setColor(Color.WHITE);
                    g2.fillOval(sliderX + filledW - 5, sliderY - 3, 16, 16);
                }
            }
        }
    }
}

