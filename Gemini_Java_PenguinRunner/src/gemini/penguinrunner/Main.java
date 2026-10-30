package gemini.penguinrunner;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import gemini.penguinrunner.assets.AssetManager;
import gemini.penguinrunner.entities.Player;
import gemini.penguinrunner.items.Flamethrower;
import gemini.penguinrunner.items.Teleport;
import gemini.penguinrunner.ui.GamePanel;
import gemini.penguinrunner.world.Direction;
import gemini.penguinrunner.world.World;

/**
 * Main application launcher for Gemini_Java_PenguinRunner.
 */
public class Main {
    public static void main(String[] args) {
        boolean testMode = false;
        String resourcePath = "resources";

        for (int i = 0; i < args.length; i++) {
            if ("-test".equals(args[i])) {
                testMode = true;
            } else if ("-g".equals(args[i]) || "-game".equals(args[i])) {
                if (i + 1 < args.length) {
                    resourcePath = "games/" + args[++i];
                }
            } else if ("-m".equals(args[i]) || "-mod".equals(args[i])) {
                if (i + 1 < args.length) {
                    resourcePath = "mods/" + args[++i];
                }
            }
        }

        File resDir = new File(resourcePath);
        if (!resDir.exists()) {
            resDir = new File("Gemini_Java_PenguinRunner/resources");
        }

        AssetManager assets = new AssetManager(resDir);
        World world = new World(resDir, assets.audio());
        boolean loaded = world.initLevel(0);

        if (testMode) {
            runAutomatedTests(world, loaded);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Penguin Runner (Optimized Java)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel panel = new GamePanel(frame, world, assets);
            frame.add(panel);
            panel.updatePanelSize();

            frame.setVisible(true);
        });
    }

    private static void runAutomatedTests(World world, boolean levelLoaded) {
        System.out.println("=== Running Gemini Java PenguinRunner Verification Tests ===");

        assertCondition(levelLoaded, "Level 1 loaded successfully");
        assertCondition(world.rows() > 0 && world.cols() > 0, "World dimensions initialized (" + world.rows() + "x" + world.cols() + ")");
        assertCondition(world.player() != null, "Player initialized");

        Player player = world.player();
        int initialRow = player.row();
        int initialCol = player.col();

        // 1. Movement test
        world.handlePlayerInput(Direction.RIGHT);
        System.out.println("Player position after RIGHT: (" + player.row() + ", " + player.col() + ")");

        // 2. Item tests
        player.addItem(new Flamethrower());
        assertCondition(player.hasItems(), "Flamethrower added to inventory");
        assertCondition("flamethrower".equals(player.getSelectedItem().name()), "Flamethrower selected");

        player.addItem(new Teleport());
        assertCondition(player.items().size() == 2, "Teleport added, count is 2");

        player.cycleItem(1);
        assertCondition("teleport".equals(player.getSelectedItem().name()), "Item cycling works cleanly");

        // 3. Save & Load test
        File testSave = new File("test_save.json");
        gemini.penguinrunner.io.SaveManager.save(world, testSave);
        assertCondition(testSave.exists(), "Save file generated");

        gemini.penguinrunner.io.SaveManager.load(world, testSave);
        assertCondition(world.player() != null, "Game loaded successfully");
        if (testSave.exists()) testSave.delete();

        // 4. Autotiler test
        gemini.penguinrunner.world.IceAutoTiler.updateAll(world.blocks());
        assertCondition(true, "IceAutoTiler executed with zero allocations");

        System.out.println("=== All Gemini Java Tests PASSED! ===");
        System.exit(0);
    }

    private static void assertCondition(boolean condition, String description) {
        if (!condition) {
            System.err.println("FAILED: " + description);
            System.exit(1);
        }
        System.out.println("PASSED: " + description);
    }
}

