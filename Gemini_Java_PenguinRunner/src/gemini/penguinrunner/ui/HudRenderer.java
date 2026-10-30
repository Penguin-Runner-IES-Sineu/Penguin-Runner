package gemini.penguinrunner.ui;

import gemini.penguinrunner.assets.AssetManager;
import gemini.penguinrunner.assets.SpriteConfig;
import gemini.penguinrunner.entities.Player;
import gemini.penguinrunner.items.Item;
import gemini.penguinrunner.world.World;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * High-performance HUD renderer.
 * Completely free of entity allocations, state mutation, and disk I/O.
 */
public class HudRenderer {
    public static final int HUD_HEIGHT = 90;

    public static void render(Graphics2D g2, World world, AssetManager assets, int panelWidth, int mapHeight, int tileSize) {
        int hudY = mapHeight;

        // Background
        g2.setColor(new Color(15, 18, 26));
        g2.fillRect(0, hudY, panelWidth, HUD_HEIGHT);

        // Top separator line
        g2.setColor(new Color(70, 110, 150));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(0, hudY, panelWidth, hudY);

        Player player = world.player();
        if (player == null) return;

        g2.setFont(assets.uiFont());
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Draw Player Lives
        SpriteConfig playerCfg = assets.getSprite("player");
        int lifeX = 16;
        int lifeY = hudY + 10;
        int iconSize = 32;

        for (int i = 0; i < player.lives(); i++) {
            if (playerCfg != null && playerCfg.image() != null) {
                g2.drawImage(playerCfg.image(), lifeX + i * (iconSize + 6), lifeY, iconSize, iconSize, null);
            } else {
                g2.setColor(Color.CYAN);
                g2.fillOval(lifeX + i * (iconSize + 6), lifeY, iconSize, iconSize);
            }
        }

        // 2. Draw Ice Cream Counter
        int iceCreamX = lifeX + 3 * (iconSize + 6) + 24;
        SpriteConfig iceCfg = assets.getSprite("icecream");
        if (iceCfg != null && iceCfg.image() != null) {
            g2.drawImage(iceCfg.image(), iceCreamX, lifeY, iconSize, iconSize, null);
        }
        g2.setColor(Color.WHITE);
        String iceText = player.collectedIceCream() + " / " + world.totalIceCream();
        g2.drawString(iceText, iceCreamX + iconSize + 8, lifeY + 22);

        // 3. Draw Level
        int levelX = iceCreamX + iconSize + 100;
        g2.setColor(new Color(255, 215, 120));
        g2.drawString("Level: " + world.currentLevel(), levelX, lifeY + 22);

        // 4. Draw Rolling 3-Slot Item Wheel (Offset -1, 0, +1)
        if (player.hasItems()) {
            List<Item> items = player.items();
            int count = items.size();
            int sel = player.selectedItemIndex();

            int wheelRight = panelWidth - 20;
            int itemSlotSize = 34;
            int spacing = 42;

            // Indicator arrow
            BufferedImage arrow = assets.getRawImage("selected");
            int arrowX = wheelRight - 3 * spacing - 16;
            if (arrow != null) {
                g2.drawImage(arrow, arrowX, lifeY + 4, 24, 24, null);
            } else {
                g2.setColor(Color.YELLOW);
                g2.drawString("▶", arrowX, lifeY + 22);
            }

            int slotIdx = 0;
            for (int offset = -1; offset <= 1; offset++) {
                int itemIdx = ((sel + offset) % count + count) % count;
                Item item = items.get(itemIdx);
                int slotX = wheelRight - (3 - slotIdx) * spacing;
                slotIdx++;

                // Selection highlight box for the active item (offset == 0)
                if (offset == 0) {
                    g2.setColor(new Color(255, 220, 100, 180));
                    g2.drawRoundRect(slotX - 2, lifeY - 2, itemSlotSize + 4, itemSlotSize + 4, 6, 6);
                }

                SpriteConfig itemCfg = assets.getSprite(item.name());
                if (itemCfg != null && itemCfg.image() != null) {
                    g2.drawImage(itemCfg.image(), slotX, lifeY, itemSlotSize, itemSlotSize, null);
                }

                // Item uses count
                g2.setFont(assets.uiFont().deriveFont(11f));
                g2.setColor(Color.WHITE);
                g2.drawString("(" + item.usesLeft() + ")", slotX + 4, lifeY + itemSlotSize + 12);
                g2.setFont(assets.uiFont());
            }
        }

        // 5. Controls reminder text
        g2.setFont(assets.uiFont().deriveFont(11f));
        g2.setColor(new Color(170, 190, 210));
        String controls = "WASD/Arrows: Move | Q/E: Dig Ice | F: Use Item | Z/X: Select Item | Esc: Pause | R: Restart";
        g2.drawString(controls, 16, hudY + 74);
    }
}

