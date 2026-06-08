/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package iessineu.penguinrunner;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author Maria Esperança
 */
public class MenuPanel extends JPanel {
    // ======
    //  MENÚ PRINCIPAL
    // ======

    private static final int W = 688;
    private static final int H = 516;
    private boolean arrancar = false;

    private final JFrame frame;

    private int seleccionat = 0;
    private Timer animTimer;
    private float cicle = 0f;
    private float alphaMenu = 0f;
    private BufferedImage imgPingui;

    private static final int PLACA_X = 174;
    private static final int PLACA_W = 320;
    private static final int ITEMS_START_Y = 270;
    private static final int ITEM_SPACING = 58;
    private static final int SUB_SPACING = 40;

    private static final Color C_JUGAR = new Color(80, 220, 255);
    private static final Color C_SORTIR = new Color(255, 110, 110);
    private static final Color[] COLORS = {C_JUGAR, C_SORTIR};
    private static final String[] LABELS = {"JUGAR", "SORTIR"};
    private boolean deathMenu = false;

    private static final int[][] ESTRELLES = {
        {60, 30}, {140, 55}, {220, 22}, {310, 48}, {400, 18}, {500, 52}, {590, 28}, {650, 70},
        {100, 95}, {280, 82}, {460, 92}, {640, 105}, {30, 115}, {180, 130}, {360, 118}, {520, 135}
    };

    public MenuPanel(JFrame frame, boolean deathMenu) {
        this.frame = frame;
        this.deathMenu = deathMenu;
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        ClassLoader classLoader = PenguinRunner.class.getClassLoader();
        // InputStream deathScreen = classLoader.getResourceAsStream("sprites/dead.png");
        InputStream deathScreen = classLoader.getResourceAsStream("sprites/enemy.png");
        if (deathMenu) {
            COLORS[0] = new Color(200, 100, 25);
            LABELS[0] = "TORNAR A JUGAR";
            setBackground(new Color(25, 25, 25));
            try {
                imgPingui = ImageIO.read(deathScreen);
            } catch (IOException ex) {
                System.getLogger(MenuPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        } else {
            setBackground(new Color(5, 20, 60));
            imgPingui = dibuixarPingui();
        }
        iniciarAnimacio();
        configurarRatolí();
    }

    private void iniciarAnimacio() {
        animTimer = new Timer(16, e -> {
            cicle += 0.03f;
            alphaMenu = Math.min(1f, alphaMenu + 0.018f);
            repaint();
        });
        animTimer.start();
    }

    private void configurarRatolí() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int[] ys = posicionsY();
                for (int i = 0; i < ys.length; i++) {
                    if (e.getX() >= PLACA_X && e.getX() <= PLACA_X + PLACA_W
                            && Math.abs(e.getY() - ys[i]) <= 20) {
                        seleccionat = i;
                        repaint();
                        break;
                    }
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int[] ys = posicionsY();
                for (int i = 0; i < ys.length; i++) {
                    if (e.getX() >= PLACA_X && e.getX() <= PLACA_X + PLACA_W
                            && Math.abs(e.getY() - ys[i]) <= 20) {
                        seleccionat = i;
                        activar();
                        break;
                    }
                }
            }
        });
    }

    private void activar() {
        if (seleccionat == 0) {
            arrancar = true;
        }
        if (seleccionat == 1) {
            System.exit(0);
        }
    }

    private int[] posicionsY() {
        int total = 2;
        int[] ys = new int[total];
        ys[0] = ITEMS_START_Y;
        ys[1] = ITEMS_START_Y + ITEM_SPACING;
        return ys;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        dibuixarFons(g2);
        dibuixarEstrelles(g2);
        dibuixarOnes(g2);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaMenu));
        dibuixarPinguiAnimat(g2);
        String titol;
        String sub;
        if (deathMenu) {
            titol = "HAS MORT";
            sub = "Millor sort la proxima vegada...";
        } else {
            titol = "PENGUIN RUNNER";
            sub = "Un projecte fet per Alumnes de l'IES Sineu";
        }
        dibuixarTitol(g2, titol, sub);
        dibuixarPlaca(g2);
        dibuixarItemsMenu(g2);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void dibuixarFons(Graphics2D g2) {
        if (deathMenu) {
            g2.setPaint(new GradientPaint(0, 0, new Color(40, 4, 4), 0, H * 0.65f, new Color(75, 8, 8)));
            g2.fillRect(0, 0, W, H);

            g2.setPaint(new GradientPaint(0, (int) (H * 0.65f), new Color(15, 15, 15), 0, H, new Color(35, 35, 35)));
            g2.fillRoundRect(-10, (int) (H * 0.65f), W + 20, (int) (H * 0.4f), 50, 50);
        } else {
            g2.setPaint(new GradientPaint(0, 0, new Color(4, 12, 45), 0, H * 0.65f, new Color(8, 35, 80)));
            g2.fillRect(0, 0, W, H);
            g2.setPaint(new GradientPaint(0, (int) (H * 0.65f), new Color(130, 190, 230), 0, H, new Color(195, 228, 255)));
            g2.fillRoundRect(-10, (int) (H * 0.65f), W + 20, (int) (H * 0.4f), 50, 50);
        }
    }

    private void dibuixarEstrelles(Graphics2D g2) {
        for (int[] est : ESTRELLES) {
            float brill = 0.5f + 0.5f * (float) Math.sin(cicle * 1.4 + est[0] * 0.07);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, brill));
            g2.setColor(Color.WHITE);
            g2.fillOval(est[0], est[1], 3, 3);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void dibuixarOnes(Graphics2D g2) {
        for (int i = 0; i < 3; i++) {
            int oy = (int) (H * 0.625f) + i * 10;
            Path2D ona = new Path2D.Float();
            ona.moveTo(0, oy);
            for (int x = 0; x <= W; x += 4) {
                double yy = oy + Math.sin(x / 55.0 + cicle * 1.6 + i) * 5;
                ona.lineTo(x, yy);
            }
            ona.lineTo(W, H);
            ona.lineTo(0, H);
            ona.closePath();
            if (deathMenu) {
                g2.setColor(new Color(15, 15, 15, 70 + i * 20));
            } else {
                g2.setColor(new Color(15, 65, 145, 70 + i * 20));
            }
            g2.fill(ona);
        }
    }

    private void dibuixarPinguiAnimat(Graphics2D g2) {
        float py = H * 0.3f + (float) Math.sin(cicle) * 7f;
        g2.drawImage(imgPingui, (int) (W * 0.68f) - 40, (int) py, 80, 110, null);
    }

    private void dibuixarTitol(Graphics2D g2, String titol, String sub) {
        Font fTitol = new Font("Courier New", Font.BOLD, 46);
        g2.setFont(fTitol);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (W - fm.stringWidth(titol)) / 2;
        g2.setColor(new Color(0, 130, 210, 70));
        g2.drawString(titol, tx + 3, 115 + 3);
        g2.setPaint(new GradientPaint(tx, 75, new Color(90, 220, 255), tx + fm.stringWidth(titol), 115, new Color(200, 255, 255)));
        g2.drawString(titol, tx, 115);
        g2.setFont(new Font("Courier New", Font.PLAIN, 14));
        g2.setColor(new Color(150, 215, 255, 170));
        g2.drawString(sub, (W - g2.getFontMetrics().stringWidth(sub)) / 2, 140);
    }

    private void dibuixarPlaca(Graphics2D g2) {
        int[] ys = posicionsY();
        int placaH = ys[ys.length - 1] - ITEMS_START_Y + 45;
        g2.setColor(new Color(0, 18, 55, 155));
        g2.fillRoundRect(PLACA_X, ITEMS_START_Y - 25, PLACA_W, placaH, 28, 28);
        g2.setColor(new Color(70, 170, 255, 55));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(PLACA_X, ITEMS_START_Y - 25, PLACA_W, placaH, 28, 28);
    }

    private void dibuixarItemsMenu(Graphics2D g2) {
        int[] ys = posicionsY();
        dibuixarBoto(g2, 0, LABELS[0], COLORS[0], ys[0]);
        dibuixarBoto(g2, 1, LABELS[1], COLORS[1], ys[1]);
    }

    private void dibuixarBoto(Graphics2D g2, int idx, String label, Color color, int y) {
        boolean sel = (seleccionat == idx);
        if (sel) {
            g2.setColor(new Color(0, 90, 190, 120));
            g2.fillRoundRect(PLACA_X + 12, y - 20, PLACA_W - 24, 42, 21, 21);
            g2.setColor(color.brighter());
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(PLACA_X + 12, y - 20, PLACA_W - 24, 42, 21, 21);
        } else {
            g2.setColor(new Color(0, 35, 90, 55));
            g2.fillRoundRect(PLACA_X + 18, y - 16, PLACA_W - 36, 34, 17, 17);
        }
        g2.setFont(new Font("Courier New", Font.BOLD, sel ? 21 : 19));
        FontMetrics fm = g2.getFontMetrics();
        int tx = PLACA_X + (PLACA_W - fm.stringWidth(label)) / 2;
        if (sel) {
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 45));
            g2.drawString(label, tx + 2, y + 9);
        }
        g2.setColor(sel ? color.brighter() : new Color(color.getRed(), color.getGreen(), color.getBlue(), 190));
        g2.drawString(label, tx, y + 7);
    }

    private BufferedImage dibuixarPingui() {
        BufferedImage img = new BufferedImage(80, 110, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(18, 18, 35));
        g.fillOval(15, 32, 50, 68);
        g.setColor(new Color(240, 248, 255));
        g.fillOval(22, 44, 36, 48);
        g.setColor(new Color(18, 18, 35));
        g.fillOval(17, 8, 46, 44);
        g.setColor(new Color(240, 248, 255));
        g.fillOval(22, 14, 36, 34);
        g.setColor(Color.BLACK);
        g.fillOval(24, 18, 10, 10);
        g.setColor(Color.WHITE);
        g.fillOval(26, 20, 4, 4);
        g.setColor(Color.BLACK);
        g.fillOval(44, 18, 10, 10);
        g.setColor(Color.WHITE);
        g.fillOval(46, 20, 4, 4);
        g.setColor(new Color(255, 160, 0));
        g.fillPolygon(new int[]{33, 47, 40}, new int[]{32, 32, 40}, 3);
        g.setColor(new Color(18, 18, 35));
        g.fillOval(4, 40, 15, 38);
        g.fillOval(61, 40, 15, 38);
        g.setColor(new Color(255, 160, 0));
        g.fillRoundRect(21, 95, 16, 9, 6, 6);
        g.fillRoundRect(43, 95, 16, 9, 6, 6);
        g.setColor(new Color(210, 40, 40));
        g.fillRoundRect(15, 46, 50, 9, 7, 7);
        g.setColor(new Color(170, 25, 25));
        g.fillRect(31, 55, 5, 14);
        g.dispose();
        return img;
    }

    public boolean isOpen() {
        return !arrancar;
    }
}
