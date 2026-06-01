/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package iessineu.penguinrunner;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;

/**
 *
 * @author Marc Mas
 */
public class Printable {

    String emoji = "X";
    Image sprite;
    Color color;
    static Font font;
    static Graphics g;

    public static final int TILE_SIZE = 43;

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String e) {
        emoji = e;
    }

    public Image getSprite() {
        return sprite;
    }

    public void setSprite(String ruta) {
        ImageIcon icon = new ImageIcon(GamePanel.getFolderPath() + "sprites/" + ruta);
        if (icon.getIconWidth() > 0) {
            sprite = icon.getImage();
        } else {
            sprite = null;
        }
    }

    public Color getColorPrintable() {
        return color;
    }

    public void setColor(int r, int g, int b) {
        color = new Color(r, g, b);
    }

    public void setColorFromHex(String hex) {
        color = new Color(Integer.decode(hex));
    }

    public Font getFont() {
        return font;
    }

    public boolean hasGraphics() {
        return g != null;
    }

    public static void setGraphics(Graphics gg) {
        g = gg;
    }

    public static void setFont(Font f) {
        font = f;
    }

    public void draw(int row, int col) {
        if (sprite != null) {
            drawImage(row, col);
        } else {
            drawEmoji(row, col);
        }
    }

    public void drawImage(int row, int col) {
        int x = col * TILE_SIZE;
        int y = row * TILE_SIZE;

        g.drawImage(sprite, x, y, TILE_SIZE, TILE_SIZE, null);
    }

    public void drawEmoji(int row, int col) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setFont(font);

        if (color != null) {
            g2.setColor(color);
        } else {
            g2.setColor(Color.WHITE);
        }

        int cellX = col * TILE_SIZE;
        int cellY = row * TILE_SIZE;

        FontRenderContext frc = g2.getFontRenderContext();
        TextLayout layout = new TextLayout(emoji, font, frc);

        Rectangle2D bounds = layout.getBounds();

        float x = (float) (cellX + (TILE_SIZE - bounds.getWidth()) / 2 - bounds.getX());

        float y = (float) (cellY + (TILE_SIZE - bounds.getHeight()) / 2 - bounds.getY());

        layout.draw(g2, x, y);
    }
}
