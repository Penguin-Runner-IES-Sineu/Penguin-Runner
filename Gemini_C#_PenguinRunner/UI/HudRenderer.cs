using System.Numerics;
using Gemini.PenguinRunner.Assets;
using Gemini.PenguinRunner.Core;
using Raylib_cs;

namespace Gemini.PenguinRunner.UI;

public static class HudRenderer
{
    public const int HudHeight = 90;

    public static void Render(World world, AssetManager assets, int panelWidth, int mapHeight, int tileSize)
    {
        int hudY = mapHeight;

        // Background
        Raylib.DrawRectangle(0, hudY, panelWidth, HudHeight, new Color(15, 18, 26, 255));
        Raylib.DrawLine(0, hudY, panelWidth, hudY, new Color(70, 110, 150, 255));

        var player = world.Player;
        if (player == null) return;

        // 1. Draw Player Lives
        var playerCfg = assets.GetSprite("player");
        int lifeX = 16;
        int lifeY = hudY + 10;
        int iconSize = 32;

        for (int i = 0; i < player.Lives; i++)
        {
            if (playerCfg?.Texture != null)
            {
                var src = new Rectangle(0, 0, playerCfg.Texture.Value.Width, playerCfg.Texture.Value.Height);
                var dst = new Rectangle(lifeX + i * (iconSize + 6), lifeY, iconSize, iconSize);
                Raylib.DrawTexturePro(playerCfg.Texture.Value, src, dst, Vector2.Zero, 0f, Color.White);
            }
            else
            {
                Raylib.DrawCircle(lifeX + i * (iconSize + 6) + iconSize / 2, lifeY + iconSize / 2, iconSize / 2f, Color.SkyBlue);
            }
        }

        // 2. Draw Ice Cream Counter
        int iceCreamX = lifeX + 3 * (iconSize + 6) + 24;
        var iceCfg = assets.GetSprite("icecream");
        if (iceCfg?.Texture != null)
        {
            var src = new Rectangle(0, 0, iceCfg.Texture.Value.Width, iceCfg.Texture.Value.Height);
            var dst = new Rectangle(iceCreamX, lifeY, iconSize, iconSize);
            Raylib.DrawTexturePro(iceCfg.Texture.Value, src, dst, Vector2.Zero, 0f, Color.White);
        }

        string iceText = $"{player.CollectedIceCream} / {world.TotalIceCream}";
        Raylib.DrawTextEx(assets.UIFont, iceText, new Vector2(iceCreamX + iconSize + 8, lifeY + 6), 18, 1, Color.White);

        // 3. Draw Level
        int levelX = iceCreamX + iconSize + 110;
        string lvlText = $"Level: {world.CurrentLevel}";
        Raylib.DrawTextEx(assets.UIFont, lvlText, new Vector2(levelX, lifeY + 6), 18, 1, new Color(255, 215, 120, 255));

        // 4. Draw Rolling 3-Slot Item Wheel
        if (player.HasItems)
        {
            var items = player.Items;
            int count = items.Count;
            int sel = player.SelectedItemIndex;

            int wheelRight = panelWidth - 20;
            int itemSlotSize = 34;
            int spacing = 42;

            var arrowTex = assets.GetRawTexture("selected");
            int arrowX = wheelRight - 3 * spacing - 16;
            if (arrowTex != null)
            {
                var src = new Rectangle(0, 0, arrowTex.Value.Width, arrowTex.Value.Height);
                var dst = new Rectangle(arrowX, lifeY + 4, 24, 24);
                Raylib.DrawTexturePro(arrowTex.Value, src, dst, Vector2.Zero, 0f, Color.White);
            }
            else
            {
                Raylib.DrawText(">", arrowX, lifeY + 6, 20, Color.Yellow);
            }

            int slotIdx = 0;
            for (int offset = -1; offset <= 1; offset++)
            {
                int itemIdx = ((sel + offset) % count + count) % count;
                var item = items[itemIdx];
                int slotX = wheelRight - (3 - slotIdx) * spacing;
                slotIdx++;

                if (offset == 0)
                {
                    Raylib.DrawRectangleLines(slotX - 2, lifeY - 2, itemSlotSize + 4, itemSlotSize + 4, new Color(255, 220, 100, 200));
                }

                var itemCfg = assets.GetSprite(item.Name);
                if (itemCfg?.Texture != null)
                {
                    var src = new Rectangle(0, 0, itemCfg.Texture.Value.Width, itemCfg.Texture.Value.Height);
                    var dst = new Rectangle(slotX, lifeY, itemSlotSize, itemSlotSize);
                    Raylib.DrawTexturePro(itemCfg.Texture.Value, src, dst, Vector2.Zero, 0f, Color.White);
                }

                string uses = $"({item.UsesLeft})";
                Raylib.DrawTextEx(assets.UIFont, uses, new Vector2(slotX + 4, lifeY + itemSlotSize + 2), 12, 1, Color.White);
            }
        }

        // 5. Controls reminder
        string controls = "WASD/Arrows: Move | Q/E: Dig Ice | F: Use Item | Z/X: Select Item | Esc: Pause | R: Restart";
        Raylib.DrawTextEx(assets.UIFont, controls, new Vector2(16, hudY + 70), 12, 1, new Color(170, 190, 210, 255));
    }
}
