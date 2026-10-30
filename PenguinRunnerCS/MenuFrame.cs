namespace iessineu.penguinrunner;

public class MenuFrame
{
    private MenuPanel menu;
    public bool arrancarJoc = false;
    public bool forceStart = false;

    public MenuFrame(bool deathMenu)
    {
        menu = new MenuPanel(deathMenu);
    }

    public bool arrancar()
    {
        return menu.arrancar_() || arrancarJoc;
    }

    public bool Arrancar() => arrancar();

    public void forceStart_()
    {
        forceStart = true;
        menu.forceStart();
    }

    public void ForceStart() => forceStart_();

    public MenuPanel getMenu() => menu;
    public MenuPanel GetMenu() => menu;
}

