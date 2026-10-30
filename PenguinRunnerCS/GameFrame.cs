namespace iessineu.penguinrunner;

public class GameFrame
{
    private GamePanel attachedPanel;

    public GameFrame()
    {
        attachedPanel = new GamePanel();
    }

    public GameFrame(GamePanel panel)
    {
        attachedPanel = panel;
    }

    public GamePanel getPanel() => attachedPanel;
    public GamePanel GetPanel() => attachedPanel;
}

