using iessineu.penguinrunner.Blocks;
using Raylib_cs;

namespace iessineu.penguinrunner.Blocks;

public class Block : Printable
{
    private bool _isSolid = false;
    private bool _isBreakable = false;
    private bool _isPushable = false;
    private bool _isClimbable = false;
    private bool _isRail = false;
    private bool _isCollectable = false;
    private bool _isDeadlyForEnemy = false;
    private bool _isDoor = false;
    private bool _isBurnable = false;
    private bool _isTrapdoor = false;
    private bool _isPressed = false;
    private bool _isFire = false;

    protected TileType type;

    public Block(TileType type)
    {
        this.type = type;

        switch (type)
        {
            case TileType.ICE:
                _isSolid = true;
                _isBreakable = true;
                break;
            case TileType.WALL:
                _isSolid = true;
                break;
            case TileType.ICECREAM:
            case TileType.FLAMETHROWER:
            case TileType.TELEPORT:
                _isCollectable = true;
                break;
            case TileType.STAIR:
                _isClimbable = true;
                break;
            case TileType.RAIL:
                _isRail = true;
                break;
            case TileType.MOLTEN:
                _isDeadlyForEnemy = true;
                break;
            case TileType.STONE:
                _isSolid = true;
                _isPushable = true;
                break;
            case TileType.WOOD:
                _isSolid = true;
                _isPushable = true;
                _isBurnable = true;
                break;
            case TileType.DOOR:
                _isDoor = true;
                this.type = TileType.BLANK;
                break;
            case TileType.TRAPDOOR:
                _isTrapdoor = true;
                this.type = TileType.BLANK;
                break;
            case TileType.BLANK:
            case TileType.EMPTY:
                _isSolid = false;
                break;
        }

        setPrintables();
    }

    public TileType getType() => type;
    public TileType GetType_() => type;

    public bool isSolid() => _isSolid;
    public bool isBreakable() => _isBreakable;
    public bool isPushable() => _isPushable;
    public bool isBurnable() => _isBurnable;
    public bool isDoor() => _isDoor;
    public bool isClimbable() => _isClimbable;
    public bool isRail() => _isRail;
    public bool isCollectable() => _isCollectable;
    public bool isDeadlyForEnemy() => _isDeadlyForEnemy;
    public bool isTrapdoor() => _isTrapdoor;
    public bool isPressed() => _isPressed;
    public bool isFire() => _isFire;

    public bool IsSolid() => _isSolid;
    public bool IsBreakable() => _isBreakable;
    public bool IsPushable() => _isPushable;
    public bool IsBurnable() => _isBurnable;
    public bool IsDoor() => _isDoor;
    public bool IsClimbable() => _isClimbable;
    public bool IsRail() => _isRail;
    public bool IsCollectable() => _isCollectable;
    public bool IsDeadlyForEnemy() => _isDeadlyForEnemy;
    public bool IsTrapdoor() => _isTrapdoor;
    public bool IsPressed() => _isPressed;
    public bool IsFire() => _isFire;

    public void setType(TileType type) => this.type = type;
    public void SetType(TileType type) => this.type = type;

    public void setPrintables()
    {
        base.setPrintables(getType().ToString().ToLowerInvariant());
    }

    public override void setPrintables(string path)
    {
        base.setPrintables(path);
    }

    public void SetPrintables() => setPrintables();
    public void SetPrintables(string path) => setPrintables(path);

    public void setDoor(bool isDoor) => _isDoor = isDoor;
    public void SetDoor(bool isDoor) => _isDoor = isDoor;

    public void setPressed(bool isPressed) => _isPressed = isPressed;
    public void SetPressed(bool isPressed) => _isPressed = isPressed;

    public void setCollectable(bool isCollectable) => _isCollectable = isCollectable;
    public void SetCollectable(bool isCollectable) => _isCollectable = isCollectable;

    public void setFire(bool isFire) => _isFire = isFire;
    public void SetFire(bool isFire) => _isFire = isFire;

    public void setSolid(bool isSolid) => _isSolid = isSolid;
    public void SetSolid(bool isSolid) => _isSolid = isSolid;
}

