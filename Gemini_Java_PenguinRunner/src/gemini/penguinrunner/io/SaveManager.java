package gemini.penguinrunner.io;

import gemini.penguinrunner.entities.Player;
import gemini.penguinrunner.items.Flamethrower;
import gemini.penguinrunner.items.Item;
import gemini.penguinrunner.items.Teleport;
import gemini.penguinrunner.world.World;

import java.io.File;
import java.nio.file.Files;

public class SaveManager {

    public static void save(World world, File file) {
        if (world == null || file == null) return;
        try {
            SimpleJson.JsonObject root = new SimpleJson.JsonObject();
            root.put("level", world.currentLevel() - 1);

            Player p = world.player();
            if (p != null) {
                root.put("lives", p.lives());
                root.put("iceCream", p.collectedIceCream());
                root.put("playerRow", p.row());
                root.put("playerCol", p.col());

                SimpleJson.JsonArray itemArr = new SimpleJson.JsonArray();
                for (Item item : p.items()) {
                    SimpleJson.JsonObject itemObj = new SimpleJson.JsonObject();
                    itemObj.put("name", item.name());
                    itemObj.put("uses", item.usesLeft());
                    itemArr.add(itemObj);
                }
                root.put("items", itemArr);
            }

            Files.writeString(file.toPath(), SimpleJson.stringify(root));
            world.audio().playSound("button");
        } catch (Exception e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }

    public static void load(World world, File file) {
        if (world == null || file == null || !file.exists()) return;
        try {
            String content = Files.readString(file.toPath());
            SimpleJson.JsonValue parsed = SimpleJson.parse(content);
            if (!(parsed instanceof SimpleJson.JsonObject root)) return;

            int level = root.getInt("level", 0);
            world.initLevel(level);

            Player p = world.player();
            if (p != null) {
                p.setLives(root.getInt("lives", Player.MAX_LIVES));
                p.setCollectedIceCream(root.getInt("iceCream", 0));
                p.setPosition(root.getInt("playerRow", p.row()), root.getInt("playerCol", p.col()));

                p.items().clear();
                SimpleJson.JsonArray items = root.getArray("items");
                if (items != null) {
                    for (SimpleJson.JsonValue iv : items) {
                        if (iv instanceof SimpleJson.JsonObject io) {
                            String name = io.getString("name", "");
                            int uses = io.getInt("uses", 1);
                            if ("flamethrower".equalsIgnoreCase(name)) {
                                p.addItem(new Flamethrower(uses));
                            } else if ("teleport".equalsIgnoreCase(name)) {
                                p.addItem(new Teleport());
                            }
                        }
                    }
                }
            }
            world.audio().playSound("button");
        } catch (Exception e) {
            System.err.println("Failed to load game: " + e.getMessage());
        }
    }
}

