namespace Gemini.PenguinRunner.Items;

using Gemini.PenguinRunner.Core;
using Gemini.PenguinRunner.Entities;

public class Teleport : IItem
{
    private bool _beaconSet;
    private int _beaconRow = -1;
    private int _beaconCol = -1;
    private bool _used;

    public string Name => "teleport";
    public int UsesLeft => _used ? 0 : (_beaconSet ? 1 : 2);
    public bool IsExpired => _used;

    public bool Use(Player player, World world)
    {
        if (_used) return false;

        if (!_beaconSet)
        {
            _beaconRow = player.Row;
            _beaconCol = player.Col;
            _beaconSet = true;
            world.Audio.PlaySound("pickup");
            return true;
        }

        player.SetPosition(_beaconRow, _beaconCol);
        _used = true;
        world.Audio.PlaySound("teleport");
        return true;
    }
}
