/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package iessineu.penguinrunner;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author Maria Esperança
 */
public class MenuPanel extends JPanel implements Serializable{

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

    private static final int PLACA_X = 148;
    private static final int PLACA_W = 392;
    private static final int ITEMS_START_Y = 250;
    private static final int ITEM_SPACING = 66;

    private static final Color C_JUGAR = new Color(80, 220, 255);
    private static final Color C_SORTIR = new Color(255, 130, 130);
    private static final Color C_REINTENTAR = new Color(255, 180, 80);
    private static final Color C_VOLUM = new Color(170, 230, 255);

    private Color[] colors;
    private String[] labels;

    private boolean deathMenu = false;

    // **** //
    private boolean obrirOpcions = false;
    private boolean menuOpcions = false;

    // **** //
    private float volum = 0.75f;
    private boolean arrossegantVolum = false;

    private static final int[][] ESTRELLES = {
        {60, 30}, {140, 55}, {220, 22}, {310, 48}, {400, 18}, {500, 52}, {590, 28}, {650, 70},
        {100, 95}, {280, 82}, {460, 92}, {640, 105}, {30, 115}, {180, 130}, {360, 118}, {520, 135}
    };

    // **** //
    private static final int[][] NEU = {
        {45, 190}, {92, 235}, {130, 175}, {205, 220}, {260, 185}, {325, 238},
        {380, 170}, {435, 225}, {510, 200}, {560, 250}, {620, 180}, {665, 235}
    };

    public MenuPanel(JFrame frame, boolean deathMenu) {
        this.frame = frame;
        this.deathMenu = deathMenu;

        setPreferredSize(new Dimension(W, H));
        setFocusable(true);

        ClassLoader classLoader = PenguinRunner.class.getClassLoader();
        InputStream deathScreen = classLoader.getResourceAsStream("sprites/enemy.png");

        if (deathMenu) {
            labels = new String[]{"Tornar a intentar", "Opcions", "Sortir"};
            colors = new Color[]{C_REINTENTAR, C_VOLUM, C_SORTIR};

            setBackground(new Color(25, 25, 25));

            try {
                imgPingui = ImageIO.read(deathScreen);
            } catch (IOException ex) {
                System.getLogger(MenuPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                imgPingui = dibuixarPingui();
            }
        } else {
            labels = new String[]{"Començar partida", "Opcions", "Sortir"};
            colors = new Color[]{C_JUGAR, C_VOLUM, C_SORTIR};

            setBackground(new Color(5, 20, 60));
            imgPingui = dibuixarPingui();
        }

        iniciarAnimacio();
        configurarRatoli();
        configurarTeclat();
    }

    private void iniciarAnimacio() {
        animTimer = new Timer(16, e -> {
            cicle += 0.03f;
            alphaMenu = Math.min(1f, alphaMenu + 0.018f);
            repaint();
        });

        animTimer.start();
    }

    private void configurarRatoli() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int[] ys = posicionsY();
                boolean sobreBoto = false;

                for (int i = 0; i < ys.length; i++) {
                    if (mouseSobreBoto(e, ys[i])) {
                        seleccionat = i;
                        sobreBoto = true;
                        repaint();
                        break;
                    }
                }

                if (menuOpcions && mouseSobreBarraVolum(e)) {
                    seleccionat = 0;
                    sobreBoto = true;
                    repaint();
                }

                setCursor(new Cursor(sobreBoto ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (menuOpcions && arrossegantVolum) {
                    actualitzarVolumAmbRatoli(e.getX());
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (menuOpcions && mouseSobreBarraVolum(e)) {
                    seleccionat = 0;
                    arrossegantVolum = true;
                    actualitzarVolumAmbRatoli(e.getX());
                    repaint();
                    return;
                }

                int[] ys = posicionsY();

                for (int i = 0; i < ys.length; i++) {
                    if (mouseSobreBoto(e, ys[i])) {
                        seleccionat = i;
                        repaint();
                        break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                arrossegantVolum = false;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (menuOpcions && mouseSobreBarraVolum(e)) {
                    seleccionat = 0;
                    actualitzarVolumAmbRatoli(e.getX());
                    repaint();
                    return;
                }

                int[] ys = posicionsY();

                for (int i = 0; i < ys.length; i++) {
                    if (mouseSobreBoto(e, ys[i])) {
                        seleccionat = i;
                        activar();
                        break;
                    }
                }
            }
        });
    }

    private void configurarTeclat() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                        seleccionat--;

                        if (seleccionat < 0) {
                            seleccionat = labels.length - 1;
                        }

                        repaint();
                    }

                    case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                        seleccionat++;

                        if (seleccionat >= labels.length) {
                            seleccionat = 0;
                        }

                        repaint();
                    }

                    case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                        if (menuOpcions && seleccionat == 0) {
                            volum = Math.max(0f, volum - 0.05f);
                            repaint();
                        }
                    }

                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                        if (menuOpcions && seleccionat == 0) {
                            volum = Math.min(1f, volum + 0.05f);
                            repaint();
                        }
                    }

                    case KeyEvent.VK_ENTER, KeyEvent.VK_SPACE -> {
                        activar();
                    }

                    case KeyEvent.VK_ESCAPE -> {
                        if (menuOpcions) {
                            frame.dispose();
                        } else {
                            seleccionat = labels.length - 1;
                            activar();
                        }
                    }

                    default -> {
                    }
                }
            }
        });
    }

    private boolean mouseSobreBoto(MouseEvent e, int y) {
        int buttonX = PLACA_X + 28;
        int buttonY = y - 24;
        int buttonW = PLACA_W - 56;
        int buttonH = 48;

        return e.getX() >= buttonX
                && e.getX() <= buttonX + buttonW
                && e.getY() >= buttonY
                && e.getY() <= buttonY + buttonH;
    }

    // **** //
    private boolean mouseSobreBarraVolum(MouseEvent e) {
        int y = posicionsY()[0];

        int barX = PLACA_X + 92;
        int barY = y + 12;
        int barW = PLACA_W - 184;
        int barH = 18;

        return e.getX() >= barX
                && e.getX() <= barX + barW
                && e.getY() >= barY - 8
                && e.getY() <= barY + barH + 8;
    }

    // **** //
    private void actualitzarVolumAmbRatoli(int mouseX) {
        int barX = PLACA_X + 92;
        int barW = PLACA_W - 184;

        float nouVolum = (mouseX - barX) / (float) barW;
        volum = Math.max(0f, Math.min(1f, nouVolum));
    }

    private void activar() {
        /*
         * Menú d'opcions:
         * 0 = Volum
         * 1 = Tornar
         */
        if (menuOpcions) {
            if (seleccionat == labels.length - 1) {
                frame.dispose();
            }

            return;
        }

        /*
         * Menú normal / menú de mort:
         * 0 = començar partida / tornar a intentar
         * 1 = opcions
         * últim = sortir
         */
        if (seleccionat == 0) {
            arrancar = true;
            return;
        }

        if (seleccionat == 1) {
            obrirOpcions = true;
            return;
        }

        if (seleccionat == labels.length - 1) {
            System.exit(0);
        }
    }

    private int[] posicionsY() {
        int total = labels.length;
        int[] ys = new int[total];

        for (int i = 0; i < total; i++) {
            ys[i] = ITEMS_START_Y + i * ITEM_SPACING;
        }

        return ys;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        dibuixarFons(g2);
        dibuixarLlumsFons(g2);
        dibuixarEstrelles(g2);
        dibuixarNeu(g2);
        dibuixarOnes(g2);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaMenu));

        dibuixarPinguiAnimat(g2);

        String titol;
        String sub;

        if (menuOpcions) {
            titol = "Opcions";
            sub = "Ajusta el joc al teu gust.";
        } else if (deathMenu) {
            titol = "Has caigut!";
            sub = "Respira, torna-ho a provar i agafa tots els gelats.";
        } else {
            titol = "Penguin Runner";
            sub = "Gel, escales, enemics i molts gelats.";
        }

        dibuixarTitol(g2, titol, sub);
        dibuixarPlaca(g2);
        dibuixarItemsMenu(g2);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void dibuixarFons(Graphics2D g2) {
        if (deathMenu) {
            g2.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            new Color(30, 4, 8),
                            0,
                            H * 0.65f,
                            new Color(82, 10, 12)
                    )
            );

            g2.fillRect(0, 0, W, H);

            g2.setPaint(
                    new GradientPaint(
                            0,
                            (int) (H * 0.65f),
                            new Color(18, 18, 18),
                            0,
                            H,
                            new Color(42, 35, 35)
                    )
            );

            g2.fillRoundRect(-10, (int) (H * 0.65f), W + 20, (int) (H * 0.4f), 50, 50);
        } else {
            g2.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            new Color(4, 12, 45),
                            0,
                            H * 0.65f,
                            new Color(8, 43, 95)
                    )
            );

            g2.fillRect(0, 0, W, H);

            g2.setPaint(
                    new GradientPaint(
                            0,
                            (int) (H * 0.65f),
                            new Color(118, 188, 232),
                            0,
                            H,
                            new Color(215, 240, 255)
                    )
            );

            g2.fillRoundRect(-10, (int) (H * 0.65f), W + 20, (int) (H * 0.4f), 50, 50);
        }
    }

    // **** //
    private void dibuixarLlumsFons(Graphics2D g2) {
        if (deathMenu) {
            g2.setPaint(
                    new RadialGradientPaint(
                            W * 0.5f,
                            H * 0.2f,
                            250,
                            new float[]{0f, 1f},
                            new Color[]{
                                new Color(255, 120, 70, 55),
                                new Color(255, 120, 70, 0)
                            }
                    )
            );
        } else {
            g2.setPaint(
                    new RadialGradientPaint(
                            W * 0.5f,
                            H * 0.2f,
                            260,
                            new float[]{0f, 1f},
                            new Color[]{
                                new Color(110, 220, 255, 60),
                                new Color(110, 220, 255, 0)
                            }
                    )
            );
        }

        g2.fillRect(0, 0, W, H);
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

    // **** //
    private void dibuixarNeu(Graphics2D g2) {
        for (int i = 0; i < NEU.length; i++) {
            int baseX = NEU[i][0];
            int baseY = NEU[i][1];

            int x = (int) (baseX + Math.sin(cicle + i) * 8);
            int y = (int) ((baseY + cicle * 18 + i * 13) % H);

            float alpha = 0.25f + 0.25f * (float) Math.sin(cicle + i * 0.8f);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(Color.WHITE);
            g2.fillOval(x, y, 4, 4);
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
                g2.setColor(new Color(20, 10, 10, 70 + i * 20));
            } else {
                g2.setColor(new Color(15, 65, 145, 70 + i * 20));
            }

            g2.fill(ona);
        }
    }

    private void dibuixarPinguiAnimat(Graphics2D g2) {
        if (imgPingui == null) {
            return;
        }

        float py = H * 0.3f + (float) Math.sin(cicle) * 7f;
        float escala = 1f + 0.025f * (float) Math.sin(cicle * 1.6f);

        int w = (int) (80 * escala);
        int h = (int) (110 * escala);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f));
        g2.setColor(Color.BLACK);
        g2.fillOval((int) (W * 0.68f) - 42, (int) py + 100, 84, 14);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaMenu));

        g2.drawImage(
                imgPingui,
                (int) (W * 0.68f) - w / 2,
                (int) py,
                w,
                h,
                null
        );
    }

    private void dibuixarTitol(Graphics2D g2, String titol, String sub) {
        Font fTitol = new Font("SansSerif", Font.BOLD, 44);

        g2.setFont(fTitol);

        FontMetrics fm = g2.getFontMetrics();
        int tx = (W - fm.stringWidth(titol)) / 2;

        if (deathMenu) {
            g2.setColor(new Color(0, 0, 0, 120));
            g2.drawString(titol, tx + 4, 115 + 4);

            g2.setPaint(
                    new GradientPaint(
                            tx,
                            75,
                            new Color(255, 170, 110),
                            tx + fm.stringWidth(titol),
                            115,
                            new Color(255, 235, 205)
                    )
            );
        } else {
            g2.setColor(new Color(0, 80, 140, 110));
            g2.drawString(titol, tx + 4, 115 + 4);

            g2.setPaint(
                    new GradientPaint(
                            tx,
                            75,
                            new Color(90, 220, 255),
                            tx + fm.stringWidth(titol),
                            115,
                            new Color(230, 255, 255)
                    )
            );
        }

        g2.drawString(titol, tx, 115);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.setColor(deathMenu ? new Color(255, 215, 190, 185) : new Color(175, 230, 255, 185));

        int subX = (W - g2.getFontMetrics().stringWidth(sub)) / 2;
        g2.drawString(sub, subX, 140);
    }

    private void dibuixarPlaca(Graphics2D g2) {
        int[] ys = posicionsY();
        int placaH = ys[ys.length - 1] - ITEMS_START_Y + 70;
        int placaY = ITEMS_START_Y - 38;

        RoundRectangle2D placa = new RoundRectangle2D.Float(
                PLACA_X,
                placaY,
                PLACA_W,
                placaH,
                34,
                34
        );

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaMenu * 0.28f));
        g2.setColor(Color.BLACK);
        g2.fillRoundRect(PLACA_X + 8, placaY + 10, PLACA_W, placaH, 34, 34);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaMenu));

        if (deathMenu) {
            g2.setPaint(
                    new GradientPaint(
                            PLACA_X,
                            placaY,
                            new Color(35, 16, 18, 205),
                            PLACA_X + PLACA_W,
                            placaY + placaH,
                            new Color(90, 28, 22, 185)
                    )
            );
        } else {
            g2.setPaint(
                    new GradientPaint(
                            PLACA_X,
                            placaY,
                            new Color(5, 28, 70, 205),
                            PLACA_X + PLACA_W,
                            placaY + placaH,
                            new Color(0, 80, 130, 175)
                    )
            );
        }

        g2.fill(placa);

        if (deathMenu) {
            g2.setColor(new Color(255, 160, 100, 105));
        } else {
            g2.setColor(new Color(95, 210, 255, 95));
        }

        g2.setStroke(new BasicStroke(2f));
        g2.draw(placa);

        g2.setColor(new Color(255, 255, 255, 35));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(PLACA_X + 8, placaY + 8, PLACA_W - 16, placaH - 16, 26, 26);
    }

    private void dibuixarItemsMenu(Graphics2D g2) {
        int[] ys = posicionsY();

        for (int i = 0; i < labels.length; i++) {
            if (menuOpcions && i == 0) {
                dibuixarControlVolum(g2, i, labels[i], colors[i], ys[i]);
            } else {
                dibuixarBoto(g2, i, labels[i], colors[i], ys[i]);
            }
        }
    }

    private void dibuixarBoto(Graphics2D g2, int idx, String label, Color color, int y) {
        boolean sel = seleccionat == idx;

        int x = PLACA_X + 28;
        int w = PLACA_W - 56;
        int h = 48;

        if (sel) {
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 45));
            g2.fillRoundRect(x - 5, y - 25, w + 10, h + 4, 26, 26);

            g2.setPaint(
                    new GradientPaint(
                            x,
                            y - 24,
                            new Color(color.getRed(), color.getGreen(), color.getBlue(), 115),
                            x + w,
                            y + 22,
                            new Color(255, 255, 255, 45)
                    )
            );

            g2.fillRoundRect(x, y - 22, w, h, 24, 24);

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 190));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x, y - 22, w, h, 24, 24);

            dibuixarFletxaSelector(g2, x - 22, y, color);
        } else {
            if (deathMenu) {
                g2.setColor(new Color(55, 25, 20, 105));
            } else {
                g2.setColor(new Color(0, 35, 90, 85));
            }

            g2.fillRoundRect(x, y - 20, w, h - 4, 22, 22);

            g2.setColor(new Color(255, 255, 255, 20));
            g2.drawRoundRect(x, y - 20, w, h - 4, 22, 22);
        }

        Font font = new Font("SansSerif", Font.BOLD, sel ? 20 : 18);
        g2.setFont(font);

        FontMetrics fm = g2.getFontMetrics();
        int tx = PLACA_X + (PLACA_W - fm.stringWidth(label)) / 2;

        g2.setColor(new Color(0, 0, 0, sel ? 120 : 75));
        g2.drawString(label, tx + 2, y + 8);

        if (sel) {
            g2.setColor(Color.WHITE);
        } else {
            g2.setColor(new Color(220, 235, 245, 220));
        }

        g2.drawString(label, tx, y + 7);
    }

    // **** //
    private void dibuixarControlVolum(Graphics2D g2, int idx, String label, Color color, int y) {
        boolean sel = seleccionat == idx;

        int x = PLACA_X + 28;
        int w = PLACA_W - 56;
        int h = 58;

        if (sel) {
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 35));
            g2.fillRoundRect(x - 5, y - 27, w + 10, h, 26, 26);

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 165));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x - 5, y - 27, w + 10, h, 26, 26);

            dibuixarFletxaSelector(g2, x - 22, y, color);
        } else {
            if (deathMenu) {
                g2.setColor(new Color(55, 25, 20, 95));
            } else {
                g2.setColor(new Color(0, 35, 90, 78));
            }

            g2.fillRoundRect(x, y - 24, w, h - 6, 24, 24);
        }

        Font font = new Font("SansSerif", Font.BOLD, 17);
        g2.setFont(font);

        String text = label + " " + Math.round(volum * 100) + "%";
        FontMetrics fm = g2.getFontMetrics();

        int tx = PLACA_X + (PLACA_W - fm.stringWidth(text)) / 2;

        g2.setColor(new Color(0, 0, 0, 100));
        g2.drawString(text, tx + 2, y - 2);

        g2.setColor(sel ? Color.WHITE : new Color(220, 235, 245, 220));
        g2.drawString(text, tx, y - 3);

        int barX = PLACA_X + 92;
        int barY = y + 12;
        int barW = PLACA_W - 184;
        int barH = 14;

        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(barX, barY, barW, barH, 12, 12);

        g2.setColor(new Color(255, 255, 255, 35));
        g2.drawRoundRect(barX, barY, barW, barH, 12, 12);

        int filledW = (int) (barW * volum);

        g2.setPaint(
                new GradientPaint(
                        barX,
                        barY,
                        new Color(80, 220, 255),
                        barX + barW,
                        barY,
                        new Color(220, 255, 255)
                )
        );

        g2.fillRoundRect(barX, barY, filledW, barH, 12, 12);

        int knobX = barX + filledW;
        int knobY = barY + barH / 2;

        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillOval(knobX - 8, knobY - 8, 18, 18);

        g2.setColor(sel ? Color.WHITE : new Color(200, 240, 255));
        g2.fillOval(knobX - 7, knobY - 7, 14, 14);

        g2.setColor(new Color(80, 190, 255));
        g2.drawOval(knobX - 7, knobY - 7, 14, 14);
    }

    // **** //
    private void dibuixarFletxaSelector(Graphics2D g2, int x, int y, Color color) {
        int offset = (int) (Math.sin(cicle * 4) * 4);

        Path2D triangle = new Path2D.Float();
        triangle.moveTo(x + offset, y - 9);
        triangle.lineTo(x + offset + 12, y);
        triangle.lineTo(x + offset, y + 9);
        triangle.closePath();

        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
        g2.fill(triangle);
    }

    private BufferedImage dibuixarPingui() {
        BufferedImage img = new BufferedImage(80, 110, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = img.createGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

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

    // **** //
    public void convertirEnMenuOpcions() {
        menuOpcions = true;
        seleccionat = 0;

        labels = new String[]{"Volum", "Tornar"};
        colors = new Color[]{C_VOLUM, C_SORTIR};

        repaint();
    }

    // **** //
    public boolean volObrirOpcions() {
        return obrirOpcions;
    }

    // **** //
    public void resetObrirOpcions() {
        obrirOpcions = false;
    }

    // **** //
    public float getVolum() {
        return volum;
    }

    public boolean isOpen() {
        return !arrancar;
    }
}