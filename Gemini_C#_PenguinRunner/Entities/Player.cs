namespace Gemini.PenguinRunner.Entities;

using Gemini.PenguinRunner.Core;
using Gemini.PenguinRunner.Items;
using Gemini.PenguinRunner.States;

public class Player : Entity
{
    public const int MaxLives = 3;

    public int Lives { get; set; } = MaxLives;
    public int CollectedIceCream { get; set; }
    public IPlayerState State { get; set; }
    public List<IItem> Items { get; } = new();
    public int SelectedItemIndex { get; private set; }

    public Player(int row, int col) : base(row, col)
    {
        State = new WalkingState();
    }

    public void LoseLife()
    {
        if (Lives > 0) Lives--;
    }

    public bool IsAlive => Lives > 0;
    public void ResetLives() => Lives = MaxLives;
    public bool HasItems => Items.Count > 0;

    public IItem? GetSelectedItem()
    {
        if (Items.Count == 0) return null;
        if (SelectedItemIndex < 0 || SelectedItemIndex >= Items.Count)
        {
            SelectedItemIndex = 0;
        }
        return Items[SelectedItemIndex];
    }

    public void CycleItem(int offset)
    {
        if (Items.Count == 0) return;
        SelectedItemIndex = ((SelectedItemIndex + offset) % Items.Count + Items.Count) % Items.Count;
    }

    public void SetSelectedItemIndex(int index)
    {
        if (Items.Count == 0) SelectedItemIndex = 0;
        else SelectedItemIndex = ((index % Items.Count) + Items.Count) % Items.Count;
    }

    public void AddItem(IItem item) => Items.Add(item);

    public void CleanExpiredItems()
    {
        Items.RemoveAll(i => i.IsExpired);
        if (SelectedItemIndex >= Items.Count && Items.Count > 0)
        {
            SelectedItemIndex = Items.Count - 1;
        }
    }

    public override void ResetToStart()
    {
        base.ResetToStart();
        State = new WalkingState();
    }
}
