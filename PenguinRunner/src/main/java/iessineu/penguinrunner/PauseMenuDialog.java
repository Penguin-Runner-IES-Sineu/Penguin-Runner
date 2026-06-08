/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author loren
 */
public class PauseMenuDialog extends JDialog {

    public static final int RESUME = 0;
    public static final int SAVE = 1;
    public static final int LOAD = 2;
    public static final int EXIT = 3;

    private int selectedOption = RESUME;
    private float volume = 0.75f;

    public PauseMenuDialog(Window owner, float currentVolume) {
        super(owner, "Pausa", ModalityType.APPLICATION_MODAL);

        this.volume = currentVolume;

        setUndecorated(true);
        setResizable(false);

        PausePanel panel = new PausePanel();
        add(panel);

        pack();
        setLocationRelativeTo(owner);
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public float getVolume() {
        return volume;
    }

    private class PausePanel extends JPanel {

        private static final int W = 688;
        private static final int H = 516;

        private static final int PLACA_X = 148;
        private static final int PLACA_W = 392;
        private static final int ITEMS_START_Y = 220;
        private static final int ITEM_SPACING = 62;

        private final String[] labels = {
            "Continuar",
            "Volum",
            "Guardar partida",
            "Carregar partida",
            "Sortir"
        };

        private final Color[] colors = {
            new Color(80, 220, 255),
            new Color(170, 230, 255),
            new Color(160, 235, 180),
            new Color(255, 215, 120),
            new Color(255, 130, 130)
        };

        private int seleccionat = 0;
        private float cicle = 0f;
        private float alphaMenu = 0f;
        private boolean arrossegantVolum = false;

        private final Timer timer;

        private final int[][] estrelles = {
            {60, 30}, {140, 55}, {220, 22}, {310, 48}, {400, 18}, {500, 52}, {590, 28}, {650, 70},
            {100, 95}, {280, 82}, {460, 92}, {640, 105}, {30, 115}, {180, 130}, {360, 118}, {520, 135}
        };

        private final int[][] neu = {
            {45, 190}, {92, 235}, {130, 175}, {205, 220}, {260, 185}, {325, 238},
            {380, 170}, {435, 225}, {510, 200}, {560, 250}, {620, 180}, {665, 235}
        };

        public PausePanel() {
            setPreferredSize(new Dimension(W, H));
            setFocusable(true);
            setBackground(Color.BLACK);

            timer = new Timer(16, e -> {
                cicle += 0.03f;
                alphaMenu = Math.min(1f, alphaMenu + 0.018f);
                repaint();
            });

            timer.start();

            configurarRatoli();
            configurarTeclat();
        }

        private void configurarRatoli() {
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    boolean sobre = false;
                    int[] ys = posicionsY();

                    for (int i = 0; i < ys.length; i++) {
                        if (mouseSobreBoto(e, ys[i])) {
                            seleccionat = i;
                            sobre = true;
                            repaint();
                            break;
                        }
                    }

                    if (mouseSobreBarraVolum(e)) {
                        seleccionat = 1;
                        sobre = true;
                        repaint();
                    }

                    setCursor(new Cursor(sobre ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (arrossegantVolum) {
                        actualitzarVolumAmbRatoli(e.getX());
                        repaint();
                    }
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (mouseSobreBarraVolum(e)) {
                        seleccionat = 1;
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
                    if (mouseSobreBarraVolum(e)) {
                        seleccionat = 1;
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
                            if (seleccionat == 1) {
                                volume = Math.max(0f, volume - 0.05f);
                                repaint();
                            }
                        }

                        case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                            if (seleccionat == 1) {
                                volume = Math.min(1f, volume + 0.05f);
                                repaint();
                            }
                        }

                        case KeyEvent.VK_ENTER, KeyEvent.VK_SPACE -> {
                            activar();
                        }

                        case KeyEvent.VK_ESCAPE -> {
                            selectedOption = RESUME;
                            tancar();
                        }

                        default -> {
                        }
                    }
                }
            });
        }

        private void activar() {
            switch (seleccionat) {
                case 0 -> {
                    selectedOption = RESUME;
                    tancar();
                }

                case 1 -> {
                    /*
                     * Volum no tanca el menú.
                     * Es modifica amb ratolí o esquerra/dreta.
                     */
                }

                case 2 -> {
                    selectedOption = SAVE;
                    tancar();
                }

                case 3 -> {
                    selectedOption = LOAD;
                    tancar();
                }

                case 4 -> {
                    selectedOption = EXIT;
                    tancar();
                }

                default -> {
                }
            }
        }

        private void tancar() {
            timer.stop();
            dispose();
        }

        private int[] posicionsY() {
            int[] ys = new int[labels.length];

            for (int i = 0; i < labels.length; i++) {
                ys[i] = ITEMS_START_Y + i * ITEM_SPACING;
            }

            return ys;
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

        private boolean mouseSobreBarraVolum(MouseEvent e) {
            int y = posicionsY()[1];

            int barX = PLACA_X + 92;
            int barY = y + 12;
            int barW = PLACA_W - 184;
            int barH = 18;

            return e.getX() >= barX
                    && e.getX() <= barX + barW
                    && e.getY() >= barY - 8
                    && e.getY() <= barY + barH + 8;
        }

        private void actualitzarVolumAmbRatoli(int mouseX) {
            int barX = PLACA_X + 92;
            int barW = PLACA_W - 184;

            float nouVolum = (mouseX - barX) / (float) barW;
            volume = Math.max(0f, Math.min(1f, nouVolum));
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

            dibuixarTitol(g2);
            dibuixarPlaca(g2);
            dibuixarItemsMenu(g2);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        private void dibuixarFons(Graphics2D g2) {
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

        private void dibuixarLlumsFons(Graphics2D g2) {
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

            g2.fillRect(0, 0, W, H);
        }

        private void dibuixarEstrelles(Graphics2D g2) {
            for (int[] est : estrelles) {
                float brill = 0.5f + 0.5f * (float) Math.sin(cicle * 1.4 + est[0] * 0.07);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, brill));
                g2.setColor(Color.WHITE);
                g2.fillOval(est[0], est[1], 3, 3);
            }

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaMenu));
        }

        private void dibuixarNeu(Graphics2D g2) {
            for (int i = 0; i < neu.length; i++) {
                int baseX = neu[i][0];
                int baseY = neu[i][1];

                int x = (int) (baseX + Math.sin(cicle + i) * 8);
                int y = (int) ((baseY + cicle * 18 + i * 13) % H);

                float alpha = 0.25f + 0.25f * (float) Math.sin(cicle + i * 0.8f);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2.setColor(Color.WHITE);
                g2.fillOval(x, y, 4, 4);
            }

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaMenu));
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

                g2.setColor(new Color(15, 65, 145, 70 + i * 20));
                g2.fill(ona);
            }
        }

        private void dibuixarTitol(Graphics2D g2) {
            String titol = "Pausa";
            String sub = "Pren-te un moment abans de continuar.";

            Font fTitol = new Font("SansSerif", Font.BOLD, 48);
            g2.setFont(fTitol);

            FontMetrics fm = g2.getFontMetrics();
            int tx = (W - fm.stringWidth(titol)) / 2;

            g2.setColor(new Color(0, 80, 140, 110));
            g2.drawString(titol, tx + 4, 105 + 4);

            g2.setPaint(
                    new GradientPaint(
                            tx,
                            70,
                            new Color(90, 220, 255),
                            tx + fm.stringWidth(titol),
                            110,
                            new Color(230, 255, 255)
                    )
            );

            g2.drawString(titol, tx, 105);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2.setColor(new Color(175, 230, 255, 185));

            int subX = (W - g2.getFontMetrics().stringWidth(sub)) / 2;
            g2.drawString(sub, subX, 132);
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

            g2.fill(placa);

            g2.setColor(new Color(95, 210, 255, 95));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(placa);

            g2.setColor(new Color(255, 255, 255, 35));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(PLACA_X + 8, placaY + 8, PLACA_W - 16, placaH - 16, 26, 26);
        }

        private void dibuixarItemsMenu(Graphics2D g2) {
            int[] ys = posicionsY();

            for (int i = 0; i < labels.length; i++) {
                if (i == 1) {
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
                g2.setColor(new Color(0, 35, 90, 85));
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

            g2.setColor(sel ? Color.WHITE : new Color(220, 235, 245, 220));
            g2.drawString(label, tx, y + 7);
        }

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
                g2.setColor(new Color(0, 35, 90, 78));
                g2.fillRoundRect(x, y - 24, w, h - 6, 24, 24);
            }

            Font font = new Font("SansSerif", Font.BOLD, 17);
            g2.setFont(font);

            String text = label + " " + Math.round(volume * 100) + "%";
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

            int filledW = (int) (barW * volume);

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
    }
}
