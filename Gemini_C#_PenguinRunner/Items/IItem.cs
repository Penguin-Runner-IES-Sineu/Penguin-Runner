namespace Gemini.PenguinRunner.Items;

using Gemini.PenguinRunner.Core;
using Gemini.PenguinRunner.Entities;

public interface IItem
{
    string Name { get; }
    int UsesLeft { get; }
    bool IsExpired { get; }
    bool Use(Player player, World world);
}
