using System.Text.Json;
using System.Text.Json.Nodes;
using Gemini.PenguinRunner.Core;
using Gemini.PenguinRunner.Entities;
using Gemini.PenguinRunner.Items;

namespace Gemini.PenguinRunner.IO;

public static class SaveManager
{
    public static void Save(World world, string filePath)
    {
        try
        {
            var dir = Path.GetDirectoryName(filePath);
            if (!string.IsNullOrEmpty(dir) && !Directory.Exists(dir))
            {
                Directory.CreateDirectory(dir);
            }

            var root = new JsonObject
            {
                ["level"] = world.CurrentLevelIndex
            };

            if (world.Player != null)
            {
                var p = world.Player;
                root["lives"] = p.Lives;
                root["iceCream"] = p.CollectedIceCream;
                root["playerRow"] = p.Row;
                root["playerCol"] = p.Col;

                var items = new JsonArray();
                foreach (var item in p.Items)
                {
                    items.Add(new JsonObject
                    {
                        ["name"] = item.Name,
                        ["uses"] = item.UsesLeft
                    });
                }
                root["items"] = items;
            }

            File.WriteAllText(filePath, root.ToJsonString(new JsonSerializerOptions { WriteIndented = true }));
            world.Audio.PlaySound("button");
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine($"Save failed: {ex.Message}");
        }
    }

    public static void Load(World world, string filePath)
    {
        if (!File.Exists(filePath)) return;

        try
        {
            string json = File.ReadAllText(filePath);
            using var doc = JsonDocument.Parse(json);
            var root = doc.RootElement;

            int level = root.TryGetProperty("level", out var lvlProp) ? lvlProp.GetInt32() : 0;
            world.InitLevel(level);

            var p = world.Player;
            if (p != null)
            {
                if (root.TryGetProperty("lives", out var livesProp)) p.Lives = livesProp.GetInt32();
                if (root.TryGetProperty("iceCream", out var iceProp)) p.CollectedIceCream = iceProp.GetInt32();
                if (root.TryGetProperty("playerRow", out var rProp) && root.TryGetProperty("playerCol", out var cProp))
                {
                    p.SetPosition(rProp.GetInt32(), cProp.GetInt32());
                }

                p.Items.Clear();
                if (root.TryGetProperty("items", out var itemsElem) && itemsElem.ValueKind == JsonValueKind.Array)
                {
                    foreach (var itemElem in itemsElem.EnumerateArray())
                    {
                        string name = itemElem.GetProperty("name").GetString() ?? "";
                        int uses = itemElem.GetProperty("uses").GetInt32();
                        if (name.Equals("flamethrower", StringComparison.OrdinalIgnoreCase))
                        {
                            p.AddItem(new Flamethrower(uses));
                        }
                        else if (name.Equals("teleport", StringComparison.OrdinalIgnoreCase))
                        {
                            p.AddItem(new Teleport());
                        }
                    }
                }
            }

            world.Audio.PlaySound("button");
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine($"Load failed: {ex.Message}");
        }
    }
}
