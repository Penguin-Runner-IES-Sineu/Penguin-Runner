package gemini.penguinrunner.assets;

import gemini.penguinrunner.io.SimpleJson;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized asset repository preloading all fonts, audio clips, and textures once.
 */
public class AssetManager {
    private final File resourceDir;
    private final AudioPlayer audio = new AudioPlayer();
    private final Map<String, SpriteConfig> spriteConfigs = new HashMap<>();
    private final Map<String, BufferedImage> rawImages = new HashMap<>();
    private Font uiFont;
    private Font emojiFont;

    public AssetManager(File resourceDir) {
        this.resourceDir = resourceDir;
        loadFonts();
        loadAudio();
        loadPrintables();
    }

    public AudioPlayer audio() { return audio; }
    public Font uiFont() { return uiFont; }
    public Font emojiFont() { return emojiFont; }

    public SpriteConfig getSprite(String key) {
        if (key == null) return null;
        return spriteConfigs.get(key.toLowerCase());
    }

    public BufferedImage getRawImage(String key) {
        return rawImages.get(key.toLowerCase());
    }

    private void loadFonts() {
        File fontFile = new File(resourceDir, "font.ttf");
        File emojiFile = new File(resourceDir, "emoji.ttf");

        try {
            if (fontFile.exists()) {
                uiFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(16f);
            } else {
                uiFont = new Font("Segoe UI", Font.BOLD, 16);
            }
        } catch (Exception e) {
            uiFont = new Font("Segoe UI", Font.BOLD, 16);
        }

        try {
            if (emojiFile.exists()) {
                emojiFont = Font.createFont(Font.TRUETYPE_FONT, emojiFile).deriveFont(28f);
            } else {
                emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 28);
            }
        } catch (Exception e) {
            emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 28);
        }
    }

    private void loadAudio() {
        String[] sfx = {
            "button", "collect", "death", "door", "flame", "gameover",
            "pickup", "popice", "teleport"
        };
        for (String name : sfx) {
            File f = new File(resourceDir, name + ".wav");
            audio.loadSound(name, f);
        }

        File music = new File(resourceDir, "music.wav");
        if (music.exists()) {
            audio.playMusic(music);
        }
    }

    private void loadPrintables() {
        File jsonFile = new File(resourceDir, "printables.json");
        if (!jsonFile.exists()) return;

        try {
            String jsonText = Files.readString(jsonFile.toPath());
            SimpleJson.JsonValue parsed = SimpleJson.parse(jsonText);

            if (parsed instanceof SimpleJson.JsonArray arr) {
                for (SimpleJson.JsonValue item : arr) {
                    if (item instanceof SimpleJson.JsonObject obj) {
                        String key = obj.getString("type", "").toLowerCase();
                        if (key.isEmpty()) continue;

                        String emoji = obj.getString("sprite", "X");
                        String hexColor = obj.getString("color", "#FFFFFF");
                        String filename = obj.getString("filename", "");

                        registerSprite(key, emoji, hexColor, filename);
                    }
                }
            } else if (parsed instanceof SimpleJson.JsonObject obj) {
                for (Map.Entry<String, SimpleJson.JsonValue> entry : obj.map().entrySet()) {
                    String key = entry.getKey().toLowerCase();
                    if (entry.getValue() instanceof SimpleJson.JsonArray arr && arr.size() >= 3) {
                        String emoji = arr.get(0).asString();
                        String hexColor = arr.get(1).asString();
                        String filename = arr.get(2).asString();
                        registerSprite(key, emoji, hexColor, filename);
                    }
                }
            }

            // Standalone utility sprites
            File selectedFile = new File(resourceDir, "sprites/selected.png");
            if (selectedFile.exists()) {
                try {
                    BufferedImage selImg = ImageIO.read(selectedFile);
                    rawImages.put("selected", selImg);
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            System.err.println("Failed to load printables.json: " + e.getMessage());
        }
    }

    private void registerSprite(String key, String emoji, String hexColor, String filename) {
        Color color = Color.WHITE;
        try {
            color = Color.decode(hexColor);
        } catch (Exception ignored) {}

        BufferedImage img = null;
        if (!filename.isEmpty()) {
            File spriteFile = new File(resourceDir, "sprites/" + filename);
            if (spriteFile.exists()) {
                try {
                    img = ImageIO.read(spriteFile);
                    rawImages.put(key, img);
                } catch (Exception ignored) {}
            }
        }

        spriteConfigs.put(key, new SpriteConfig(emoji, color, img));
    }
}

