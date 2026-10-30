using System.Text.Json;
using iessineu.penguinrunner.Blocks;
using iessineu.penguinrunner.Entity;
using iessineu.penguinrunner.Entity.Enemies;
using iessineu.penguinrunner.Entity.Items;
using iessineu.penguinrunner.Movement;
using iessineu.penguinrunner.States;

namespace iessineu.penguinrunner;

public class GameState
{
    private List<PositionHistory> lastPositions = new();
    private bool gameOver = false;
    private List<BrokenBlock> brokenBlocks = new();
    private List<Block> stones = new();
    private string rutaMapes = GamePanel.getFolderPath() + "maps.json";
    private int nivellActual = 0;
    private readonly AI buscador = new();
    private List<GameMap> mapList;
    private GameMap mapObject;
    private Player player = null!;
    private List<Enemy> enemies = new();
    private int iceCream = 0;
    private int lives = 3;
    private int startPlayerRow;
    private int startPlayerCol;
    private bool moddedMusic = false;
    private bool buttonPressed = false;
    private float gameVolume = 0.95f;
    private bool isMuted;

    private SoundManager musicManager = new();
    private SoundManager soundFXManager = new();
    private PlayerState walkingState = new WalkingState();
    private PlayerState climbingState = new ClimbingState();
    private PlayerState railState = new RailState();
    private PlayerState fallingState = new FallingState();

    private const int NOT_VISITED = -1;
    private const int BLOCKED = -2;

    private Block[][] blocks = null!;

    // Falling timer emulation for Raylib
    private bool fallingTimerRunning = false;
    private float fallingTimerElapsed = 0f;
    private const float FALLING_INTERVAL = 0.150f;

    public GameState()
    {
        rutaMapes = GamePanel.getFolderPath() + "maps.json";
        mapList = llegirMapes(rutaMapes);
        if (mapList.Count == 0)
        {
            // Fallback basic map if maps.json couldn't be loaded
            mapList.Add(new GameMap(new[]
            {
                "###############################",
                "# P                           #",
                "#.............................#",
                "###############################"
            }));
        }
        mapObject = mapList[0];
        loadMap();
    }

    public void resetTimer()
    {
        fallingTimerRunning = false;
        fallingTimerElapsed = 0f;
    }

    public void update(float dt)
    {
        musicManager.updateMusic();

        if (fallingTimerRunning && player.getState() == fallingState)
        {
            fallingTimerElapsed += dt;
            if (fallingTimerElapsed >= FALLING_INTERVAL)
            {
                fallingTimerElapsed = 0f;
                movePlayerDownOne();
                updatePlayerState();
                updateLogic();
                takeTurn();
            }
        }
    }

    public Block[][] loadMap()
    {
        resetTimer();
        List<Item> savedItemList = new();
        if (player != null)
        {
            savedItemList = player.getItems();
        }

        string[] level = mapObject.getMap();
        blocks = new Block[level.Length][];
        brokenBlocks = new List<BrokenBlock>();
        stones = new List<Block>();
        enemies = new List<Enemy>();
        player = null!;
        startPlayerRow = 0;
        startPlayerCol = 0;
        iceCream = 0;
        buttonPressed = false;

        for (int row = 0; row < level.Length; row++)
        {
            blocks[row] = new Block[level[row].Length];
            for (int col = 0; col < level[row].Length; col++)
            {
                char symbol = level[row][col];
                switch (symbol)
                {
                    case '#':
                        blocks[row][col] = new Block(TileType.WALL);
                        break;
                    case '.':
                        blocks[row][col] = new Block(TileType.ICE);
                        break;
                    case 'H':
                        blocks[row][col] = new Block(TileType.STAIR);
                        break;
                    case '-':
                        blocks[row][col] = new Block(TileType.RAIL);
                        break;
                    case 'D':
                        blocks[row][col] = new Block(TileType.DOOR);
                        break;
                    case 'S':
                        blocks[row][col] = new Block(TileType.STONE);
                        stones.Add(blocks[row][col]);
                        break;
                    case 'B':
                        blocks[row][col] = new Block(TileType.BUTTON);
                        break;
                    case 'b':
                        blocks[row][col] = new Block(TileType.TRAPDOOR);
                        break;
                    case 'G':
                        blocks[row][col] = new Block(TileType.ICECREAM);
                        iceCream++;
                        break;
                    case 'F':
                        blocks[row][col] = new Block(TileType.FLAMETHROWER);
                        break;
                    case 'T':
                        blocks[row][col] = new Block(TileType.TELEPORT);
                        break;
                    case 'P':
                        player = new Player(row, col);
                        startPlayerRow = row;
                        startPlayerCol = col;
                        blocks[row][col] = new Block(TileType.BLANK);
                        saveLastPosition();
                        break;
                    case 'E':
                        enemies.Add(new Enemy(row, col, 1, 1));
                        blocks[row][col] = new Block(TileType.BLANK);
                        break;
                    case 'A':
                        enemies.Add(new AmbushingEnemy(row, col, 1, 1));
                        blocks[row][col] = new Block(TileType.BLANK);
                        break;
                    case 'W':
                        enemies.Add(new SeekerEnemy(row, col, 1, 1));
                        blocks[row][col] = new Block(TileType.BLANK);
                        break;
                    case 'C':
                        enemies.Add(new IceCreamEnemy(row, col, 1, 1));
                        blocks[row][col] = new Block(TileType.BLANK);
                        break;
                    default:
                        blocks[row][col] = new Block(TileType.BLANK);
                        break;
                }
            }
        }

        if (player == null)
        {
            player = new Player(0, 0);
        }

        player.setItems(savedItemList);
        updatePlayerState();
        updateTrapdoors();
        updateDoors();

        return blocks;
    }

    public Block[][] LoadMap() => loadMap();

    private int[][] createPathMapForAI(Enemy currentEnemy)
    {
        int[][] pathMap = new int[getRows()][];

        for (int row = 0; row < getRows(); row++)
        {
            pathMap[row] = new int[getCols()];
            for (int col = 0; col < getCols(); col++)
            {
                bool occupiedByAnotherEnemy = isEnemy(row, col)
                        && !(currentEnemy.getRow() == row && currentEnemy.getCol() == col);

                bool molten = isMolten(row, col);

                if ((isSolid(row, col) && !molten) || occupiedByAnotherEnemy)
                {
                    pathMap[row][col] = BLOCKED;
                }
                else
                {
                    pathMap[row][col] = NOT_VISITED;
                }
            }
        }

        return pathMap;
    }

    public void reloadSprites()
    {
        resetTimer();
        mapObject = mapList[nivellActual];
        Block[][] mapa = getBlocks();
        for (int row = 0; row < mapa.Length; row++)
        {
            for (int col = 0; col < mapa[row].Length; col++)
            {
                Block blocAntic = mapa[row][col];
                blocAntic.setPrintables();
            }
        }

        foreach (Enemy enemy in getEnemies())
        {
            enemy.setPrintables();
        }

        player.setPrintables();
        updatePlayerState();
    }

    public void ReloadSprites() => reloadSprites();

    /*
     * TORNS
     */
    public void takeTurn(Direction? direction)
    {
        if (player.getState() == fallingState)
        {
            if (!fallingTimerRunning)
            {
                fallingTimerRunning = true;
                fallingTimerElapsed = 0f;
            }
        }
        else if (direction != null)
        {
            fallingTimerRunning = false;
            player.setLastDirection(direction);
            player.getState().handleInput(this, direction);
            updatePlayerState();
            updateLogic();
            if (player.getState() == fallingState)
            {
                takeTurn();
            }
        }
    }

    public void TakeTurn(Direction? direction) => takeTurn(direction);

    public void changeItem(bool isLeft)
    {
        if (player.hasItems())
        {
            if (isLeft)
            {
                player.previousItem();
            }
            else
            {
                player.nextItem();
            }
            Console.WriteLine("Item seleccionat: " + player.getSelectedItem().getName());
        }
        else
        {
            Console.WriteLine("No tens cap item!");
        }
    }

    public void ChangeItem(bool isLeft) => changeItem(isLeft);

    public void takeTurn()
    {
        takeTurn(null);
    }

    public void TakeTurn() => takeTurn();

    public void updateLogic()
    {
        saveLastPosition();
        collectItem();
        moveBlocks();
        updateBrokenBlocks();
        moveEnemies();
        checkCollisions();
        updatePlayerState();
        updateDoors();
        updateTrapdoors();
    }

    public void UpdateLogic() => updateLogic();

    public void useItem()
    {
        Item i = player.getSelectedItem();
        if (player.hasItems())
        {
            player.useItem(this);
        }
        else
        {
            Console.WriteLine("No tens objectes!");
            return;
        }

        if (i == null || string.IsNullOrEmpty(i.getName()))
        {
            return;
        }

        switch (i.getName().ToLowerInvariant())
        {
            case "flamethrower":
                soundFXManager.playSound("flame", !GamePanel.hasGame());
                break;
            case "teleport":
                Teleport t = (Teleport)i;
                bool moved = t.use(player);
                if (moved)
                {
                    blocks[t.getRow()][t.getCol()] = new Block(TileType.BLANK);
                    soundFXManager.playSound("teleport", !GamePanel.hasGame());
                    player.removeItem();
                    player.setSelectedItemIndex(0);
                }
                else
                {
                    blocks[t.getRow()][t.getCol()] = new Block(TileType.TELEPORT);
                    blocks[t.getRow()][t.getCol()].setCollectable(false);
                }
                blocks[t.getRow()][t.getCol()].setPrintables();
                break;
        }
    }

    public void UseItem() => useItem();

    /*
     * MÀQUINA D'ESTATS DEL JUGADOR
     */
    private void updatePlayerState()
    {
        if (shouldPlayerDrop())
        {
            player.setState(fallingState);
            fallingTimerRunning = true;
        }
        else if (isStair(player.getRow(), player.getCol()))
        {
            player.setState(climbingState);
            fallingTimerRunning = false;
        }
        else if (isRail(player.getRow(), player.getCol()))
        {
            player.setState(railState);
            fallingTimerRunning = false;
        }
        else
        {
            player.setState(walkingState);
            fallingTimerRunning = false;
        }
    }

    public bool isPlayerOnStair()
    {
        return isStair(player.getRow(), player.getCol());
    }

    public bool IsPlayerOnStair() => isPlayerOnStair();

    public void movePlayerBy(Direction direction, bool canPushStone)
    {
        int row = player.getRow();
        int col = player.getCol();

        int nextRow = row + direction.getDr();
        int nextCol = col + direction.getDc();

        if (canPushStone && isStone(nextRow, nextCol))
        {
            bool pushed = tryPushStone(row, col, direction);
            if (!pushed)
            {
                return;
            }
        }

        if (canMoveTo(nextRow, nextCol))
        {
            player.setPosition(nextRow, nextCol);
        }
    }

    public void MovePlayerBy(Direction direction, bool canPushStone) => movePlayerBy(direction, canPushStone);

    public void movePlayerDownOne()
    {
        int nextRow = player.getRow() + 1;
        int nextCol = player.getCol();

        if (canMoveTo(nextRow, nextCol))
        {
            player.setPosition(nextRow, nextCol);
        }
    }

    public void MovePlayerDownOne() => movePlayerDownOne();

    private bool shouldPlayerDrop()
    {
        int row = player.getRow();
        int col = player.getCol();
        if (isEnemy(row + 1, col))
        {
            return false;
        }

        return !isSolid(row + 1, col)
                && !isRail(row, col)
                && !isStair(row, col)
                && !isStair(row + 1, col);
    }

    /*
     * PEDRES
     */
    private bool tryPushStone(int row, int playerCol, Direction direction)
    {
        int dc = direction.getDc();

        if (dc == 0)
        {
            return false;
        }

        int firstStoneCol = playerCol + dc;
        int checkCol = firstStoneCol;

        while (!isOutOfBounds(row, checkCol) && isStone(row, checkCol))
        {
            checkCol += dc;
        }

        if (!canStoneMoveTo(row, checkCol))
        {
            return false;
        }

        int col = checkCol - dc;

        while (col != playerCol)
        {
            Block blockToMove = blocks[row][col];
            blocks[row][col + dc] = blockToMove;

            blocks[row][col] = getBlockAfterStoneLeaves(row, col);

            col -= dc;
        }

        return true;
    }

    private void moveBlocks()
    {
        for (int row = blocks.Length - 1; row >= 0; row--)
        {
            for (int col = 0; col < blocks[row].Length; col++)
            {
                if (blocks[row][col].getType() == TileType.STONE)
                {
                    int nextRow = row + 1;

                    if (isOutOfBounds(nextRow, col))
                    {
                        continue;
                    }

                    if (!canStoneMoveTo(nextRow, col))
                    {
                        continue;
                    }

                    if (player.getRow() == nextRow && player.getCol() == col)
                    {
                        continue;
                    }

                    if (isEnemy(nextRow, col))
                    {
                        continue;
                    }

                    blocks[nextRow][col] = blocks[row][col];
                    blocks[row][col] = getBlockAfterStoneLeaves(row, col);
                }
            }
        }
    }

    /*
     * ACCIONS
     */
    public void breakDownLeft()
    {
        breakBlock(player.getRow() + 1, player.getCol() - 1);
        updateLogic();
    }

    public void BreakDownLeft() => breakDownLeft();

    public void breakDownRight()
    {
        breakBlock(player.getRow() + 1, player.getCol() + 1);
        updateLogic();
    }

    public void BreakDownRight() => breakDownRight();

    public void breakBlock(int row, int col)
    {
        if (isOutOfBounds(row, col))
        {
            return;
        }

        Block block = blocks[row][col];

        if (block != null && block.isBreakable())
        {
            soundFXManager.playSound("popice", !GamePanel.hasGame());
            blocks[row][col] = new Block(TileType.MOLTEN);
            brokenBlocks.Add(new BrokenBlock(row, col, 5));
        }
    }

    public void BreakBlock(int row, int col) => breakBlock(row, col);

    private void updateBrokenBlocks()
    {
        for (int i = brokenBlocks.Count - 1; i >= 0; i--)
        {
            BrokenBlock block = brokenBlocks[i];

            if (block.turnsLeft <= 1 && isStone(block.row, block.col))
            {
                block.turnsLeft = 1;
                continue;
            }

            block.turnsLeft--;

            if (block.turnsLeft <= 0 && isStone(block.row, block.col))
            {
                block.turnsLeft = 1;
                continue;
            }

            if (block.turnsLeft <= 0)
            {
                blocks[block.row][block.col] = new Block(TileType.ICE);
                brokenBlocks.RemoveAt(i);
            }
        }
    }

    /*
     * ENEMICS
     */
    private void moveEnemies()
    {
        foreach (Enemy enemy in enemies)
        {
            if (enemy.isDead())
            {
                enemy.subtractTimeToRevive(1);

                if (enemy.getTimeToRevive() <= 0)
                {
                    PositionHistory? respawnPosition = findEnemyRespawnPosition(enemy);

                    if (respawnPosition != null)
                    {
                        enemy.setPosition(respawnPosition.getX(), respawnPosition.getY());
                        enemy.revive();
                    }
                }

                continue;
            }

            if (!isMolten(enemy.getRow(), enemy.getCol()))
            {
                moveEnemy(enemy);
            }
        }
    }

    private void moveEnemy(Enemy enemy)
    {
        int row = enemy.getRow();
        int col = enemy.getCol();

        if (shouldDie(row, col))
        {
            dropIceCreamIfNeeded(enemy);
            enemy.die();
            enemy.setTimeToRevive(7);
            return;
        }

        if (shouldEnemyDrop(row, col))
        {
            if (!isEnemy(row + 1, col))
            {
                enemy.setPosition(row + 1, col);
            }
            return;
        }

        if (enemy is IceCreamEnemy iceCreamEnemy)
        {
            moveIceCreamEnemy(iceCreamEnemy);
            return;
        }

        if (enemy is AmbushingEnemy ambushingEnemy)
        {
            int enemyRow = ambushingEnemy.getRow();
            int enemyCol = ambushingEnemy.getCol();

            PositionHistory? easyTarget = canReachEasy(
                    enemyRow,
                    enemyCol,
                    player.getRow(),
                    player.getCol()
            );

            PositionHistory target;
            if (easyTarget != null)
            {
                target = easyTarget;
            }
            else
            {
                target = getAmbushingTarget();
            }

            Direction? direction = buscador.getShortestDirection(
                    createPathMapForAI(ambushingEnemy),
                    createTileMapForAI(),
                    enemyRow,
                    enemyCol,
                    target.getX(),
                    target.getY()
            );

            if (direction == null)
            {
                return;
            }

            int nextRow = ambushingEnemy.getRow() + direction.getDr();
            int nextCol = ambushingEnemy.getCol() + direction.getDc();

            if (isEnemy(nextRow, nextCol))
            {
                return;
            }

            if ((canMoveTo(nextRow, nextCol) && !isEnemy(nextRow, nextCol))
                    || isMolten(nextRow, nextCol))
            {
                ambushingEnemy.setPosition(nextRow, nextCol);
            }

            return;
        }

        if (enemy is SeekerEnemy seekerEnemy)
        {
            Direction? direction = buscador.getShortestDirection(
                    createPathMapForAI(seekerEnemy),
                    createTileMapForAI(),
                    seekerEnemy.getRow(),
                    seekerEnemy.getCol(),
                    player.getRow(),
                    player.getCol()
            );

            if (direction == null)
            {
                return;
            }

            int nextRow = seekerEnemy.getRow() + direction.getDr();
            int nextCol = seekerEnemy.getCol() + direction.getDc();

            if (isEnemy(nextRow, nextCol))
            {
                return;
            }

            if ((canMoveTo(nextRow, nextCol) || isMolten(nextRow, nextCol))
                    && !isEnemy(nextRow, nextCol))
            {
                seekerEnemy.setPosition(nextRow, nextCol);
            }

            return;
        }

        moveBasicEnemy(enemy);
    }

    private bool shouldEnemyDrop(int row, int col)
    {
        return !isSolid(row + 1, col)
                && !isRail(row, col)
                && !isStair(row, col)
                && !isStair(row + 1, col)
                && !isEnemy(row + 1, col);
    }

    private bool shouldDie(int row, int col)
    {
        return isIce(row, col);
    }

    /*
     * OBJECTES I COL·LISIONS
     */
    private void collectItem()
    {
        int row = player.getRow();
        int col = player.getCol();

        Block block = blocks[row][col];

        if (block != null && block.isCollectable())
        {
            if (!player.hasItem(block.getType().ToString().ToLowerInvariant()))
            {
                if (block.getType() != TileType.ICECREAM)
                {
                    soundFXManager.playSound("pickup", !GamePanel.hasGame());
                }
                blocks[row][col] = new Block(TileType.BLANK);
                switch (block.getType())
                {
                    case TileType.ICECREAM:
                        soundFXManager.playSound("collect", !GamePanel.hasGame());
                        player.addIceCream();
                        updateDoors();
                        break;
                    case TileType.FLAMETHROWER:
                        player.addItem("flamethrower");
                        break;
                    case TileType.TELEPORT:
                        player.addItem("teleport");
                        break;
                }
            }
        }
    }

    private void checkCollisions()
    {
        int playerRow = player.getRow();
        int playerCol = player.getCol();
        bool dead = false;

        foreach (Enemy enemy in enemies)
        {
            if (!enemy.isDead() && isEnemy(playerRow, playerCol))
            {
                dead = true;
            }
            if (!enemy.isDead() && isEnemy(playerRow, playerCol) && blocks[enemy.getRow()][enemy.getCol()].getType() != TileType.MOLTEN)
            {
                dead = true;
            }
            if (dead)
            {
                Console.WriteLine("You died");
                playerDied(false);
                return;
            }
        }

        if (isIce(playerRow, playerCol) || isFire(playerRow, playerCol))
        {
            Console.WriteLine("You died");
            playerDied(false);
        }
    }

    public void playerDied(bool wasForced)
    {
        player.loseLife();

        if (player.isAlive())
        {
            if (!wasForced)
            {
                soundFXManager.playSound("death", !GamePanel.hasGame());
            }
            int savedLives = player.getLives();
            loadMap();
            player.setItems(new List<Item>());
            player.setLives(savedLives);
        }
        else
        {
            soundFXManager.playSound("gameover", !GamePanel.hasGame());
            gameOver_();
        }
    }

    public void PlayerDied(bool wasForced) => playerDied(wasForced);

    private void gameOver_()
    {
        nivellActual = 0;
        mapObject = mapList[0];
        loadMap();
        setGameOver(true);
        player.setItems(new List<Item>());
    }

    public void GameOver() => gameOver_();

    /*
     * CONSULTES DE BLOCS
     */
    private bool canStoneMoveTo(int row, int col)
    {
        if (isOutOfBounds(row, col))
        {
            return false;
        }

        return isBlank(row, col) || isMolten(row, col);
    }

    private bool isBrokenBlockAt(int row, int col)
    {
        foreach (BrokenBlock block in brokenBlocks)
        {
            if (block.row == row && block.col == col)
            {
                return true;
            }
        }

        return false;
    }

    private Block getBlockAfterStoneLeaves(int row, int col)
    {
        if (isBrokenBlockAt(row, col))
        {
            return new Block(TileType.MOLTEN);
        }

        return new Block(TileType.BLANK);
    }

    private bool canMoveTo(int row, int col)
    {
        if (isOutOfBounds(row, col))
        {
            return false;
        }

        if (blocks[row][col].getType() == TileType.MOLTEN && isEnemy(row, col))
        {
            return false;
        }

        return !isSolid(row, col);
    }

    private bool isOutOfBounds(int row, int col)
    {
        return row < 0
                || row >= getRows()
                || col < 0
                || col >= getCols();
    }

    private Block getBlock(int row, int col)
    {
        if (isOutOfBounds(row, col))
        {
            return new Block(TileType.BLANK);
        }

        return blocks[row][col];
    }

    private bool isBlank(int row, int col)
    {
        return !isOutOfBounds(row, col) && blocks[row][col].getType() == TileType.BLANK;
    }

    public bool isSolid(int row, int col)
    {
        if (isOutOfBounds(row, col))
        {
            return true;
        }

        Block block = blocks[row][col];
        return block != null && block.isSolid();
    }

    public bool IsSolid(int row, int col) => isSolid(row, col);

    public bool isIce(int row, int col)
    {
        Block block = getBlock(row, col);
        return block != null && block.getType() == TileType.ICE;
    }

    public bool IsIce(int row, int col) => isIce(row, col);

    public bool isFire(int row, int col)
    {
        Block block = getBlock(row, col);
        return block.isFire();
    }

    public bool IsFire(int row, int col) => isFire(row, col);

    public bool isRail(int row, int col)
    {
        Block block = getBlock(row, col);
        return block != null && block.isRail();
    }

    public bool IsRail(int row, int col) => isRail(row, col);

    public bool isStair(int row, int col)
    {
        Block block = getBlock(row, col);
        return block != null && block.isClimbable();
    }

    public bool IsStair(int row, int col) => isStair(row, col);

    public bool isStone(int row, int col)
    {
        Block block = getBlock(row, col);
        return block != null && block.isPushable();
    }

    public bool IsStone(int row, int col) => isStone(row, col);

    public bool isMolten(int row, int col)
    {
        Block block = getBlock(row, col);
        return block != null && block.getType() == TileType.MOLTEN;
    }

    public bool IsMolten(int row, int col) => isMolten(row, col);

    public bool isEnemy(int row, int col)
    {
        foreach (Enemy enemy in enemies)
        {
            if (!enemy.isDead()
                    && enemy.getRow() == row
                    && enemy.getCol() == col)
            {
                return true;
            }
        }

        return false;
    }

    public bool IsEnemy(int row, int col) => isEnemy(row, col);

    /*
     * GETTERS
     */
    public TileType getType(int row, int col) => getBlock(row, col).getType();
    public TileType GetType_(int row, int col) => getType(row, col);

    public int getRows() => blocks.Length;
    public int getCols() => blocks.Length > 0 ? blocks[0].Length : 0;

    public int GetRows() => getRows();
    public int GetCols() => getCols();

    public Player getPlayer() => player;
    public Player GetPlayer() => player;

    public List<Enemy> getEnemies() => enemies;
    public List<Enemy> GetEnemies() => enemies;

    public Block[][] getBlocks() => blocks;
    public Block[][] GetBlocks() => blocks;

    public void setBlock(int row, int col, Block block)
    {
        blocks[row][col] = block;
    }

    public void SetBlock(int row, int col, Block block) => setBlock(row, col, block);

    public void interact()
    {
        Block bloc = blocks[player.getRow()][player.getCol()];
        if (bloc.getType() == TileType.DOOR || (bloc.isDoor() && checkObjective()))
        {
            soundFXManager.playSound("door", !GamePanel.hasGame());
            nivellActual++;
            if (nivellActual >= mapList.Count)
            {
                nivellActual = 0;
            }
            mapObject = mapList[nivellActual];
            loadMap();
            return;
        }
        if (bloc.getType() == TileType.BUTTON)
        {
            buttonPressed = !buttonPressed;
            soundFXManager.playSound("button", !GamePanel.hasGame());
            updateTrapdoors();
        }
    }

    public void Interact() => interact();

    public void updateDoors()
    {
        if (checkObjective())
        {
            for (int r = 0; r < getRows(); r++)
            {
                for (int c = 0; c < getCols(); c++)
                {
                    Block b = blocks[r][c];
                    if (b != null && b.isDoor())
                    {
                        b.setType(TileType.DOOR);
                        b.setPrintables();
                    }
                }
            }
        }
    }

    public void UpdateDoors() => updateDoors();

    public void updateTrapdoors()
    {
        for (int r = 0; r < getRows(); r++)
        {
            for (int c = 0; c < getCols(); c++)
            {
                Block b = blocks[r][c];
                if (b != null && b.isTrapdoor())
                {
                    if (buttonPressed)
                    {
                        b.setType(TileType.TRAPDOOR);
                        b.setSolid(true);
                    }
                    else
                    {
                        b.setType(TileType.BLANK);
                        b.setSolid(false);
                    }
                    b.setPrintables();
                }
            }
        }
    }

    public void UpdateTrapdoors() => updateTrapdoors();

    public bool checkObjective()
    {
        return player.geticeCream() >= iceCream;
    }

    public bool CheckObjective() => checkObjective();

    public bool buttonPressed_() => buttonPressed;
    public bool ButtonPressed() => buttonPressed;

    public int getNivell() => nivellActual;
    public int GetNivell() => nivellActual;

    public List<Block> getStones() => stones;
    public void setStones(List<Block> stones) => this.stones = stones;

    public List<GameMap> getMapList() => mapList;
    public void setMapList(List<GameMap> mapList) => this.mapList = mapList;

    public bool isModdedMusic() => moddedMusic;
    public void setModdedMusic(bool moddedMusic) => this.moddedMusic = moddedMusic;

    public void muteMusic()
    {
        musicManager.setVolume(0f);
        isMuted = true;
    }

    public void MuteMusic() => muteMusic();

    public void unmuteMusic()
    {
        isMuted = false;
        musicManager.setVolume(gameVolume);
    }

    public void UnmuteMusic() => unmuteMusic();

    public void changeVolume(float vol)
    {
        if (vol > 1f)
        {
            vol = vol / 100f;
        }

        vol = Math.Clamp(vol, 0f, 1f);
        gameVolume = vol;

        if (!isMuted)
        {
            musicManager.setVolume(gameVolume);
        }
    }

    public void ChangeVolume(float vol) => changeVolume(vol);

    public float getGameVolume() => gameVolume;
    public float GetGameVolume() => gameVolume;

    public bool isGameOver() => gameOver;
    public void setGameOver(bool gameOver) => this.gameOver = gameOver;

    public bool IsGameOver() => gameOver;
    public void SetGameOver(bool gameOver) => setGameOver(gameOver);

    public bool isMuted_() => isMuted;
    public bool IsMuted() => isMuted;

    /*
     * BLOCS ROMPUTS
     */
    private class BrokenBlock
    {
        public int row;
        public int col;
        public int turnsLeft;

        public BrokenBlock(int row, int col, int turnsLeft)
        {
            this.row = row;
            this.col = col;
            this.turnsLeft = turnsLeft;
        }
    }

    /*
     * LECTURA MAPES
     */
    public List<GameMap> llegirMapes(string ruta)
    {
        List<GameMap> llista = new();
        string json = llegirMapaJSON(!GamePanel.hasGame());

        if (string.IsNullOrWhiteSpace(json))
        {
            return llista;
        }

        try
        {
            using var doc = JsonDocument.Parse(json);
            var root = doc.RootElement;
            if (root.ValueKind == JsonValueKind.Array)
            {
                foreach (var obj in root.EnumerateArray())
                {
                    int mapIndex = obj.GetProperty("level").GetInt32() - 1;
                    var viewElem = obj.GetProperty("view");
                    List<string> viewList = new();
                    foreach (var line in viewElem.EnumerateArray())
                    {
                        viewList.Add(line.GetString() ?? "");
                    }

                    GameMap map = new GameMap(viewList.ToArray());
                    while (llista.Count <= mapIndex)
                    {
                        llista.Add(null!);
                    }
                    llista[mapIndex] = map;
                }
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine("Error llegint JSON mapes: " + ex.Message);
        }

        // Remove any null placeholders
        llista.RemoveAll(item => item == null);
        return llista;
    }

    public List<GameMap> LlegirMapes(string ruta) => llegirMapes(ruta);

    public string llegirMapaJSON(bool fromResource)
    {
        Console.Write("Carregant Mapes:");
        string path = fromResource
            ? Path.Combine("resources", "maps.json")
            : Path.Combine(GamePanel.getFolderPath(), "maps.json");

        if (!File.Exists(path))
        {
            string alt = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, path);
            if (File.Exists(alt)) path = alt;
            else
            {
                alt = Path.Combine(Directory.GetCurrentDirectory(), "PenguinRunnerCS", path);
                if (File.Exists(alt)) path = alt;
            }
        }

        try
        {
            if (fromResource)
            {
                Console.WriteLine(" Carregant Recurs (" + path + ")");
            }
            else
            {
                Console.WriteLine(" Carregant Fitxer (" + path + ")");
            }

            if (File.Exists(path))
            {
                return File.ReadAllText(path);
            }
        }
        catch (Exception e)
        {
            Console.WriteLine(e.Message);
        }

        return "";
    }

    public string LlegirMapaJSON(bool fromResource) => llegirMapaJSON(fromResource);

    public void addMod(string modType, JsonElement mod, string modPath)
    {
        switch (modType)
        {
            case "map":
                addModMap(mod);
                break;
            case "sprite":
                addModSprite(mod, modPath);
                break;
            case "sfx":
                addModSound(mod, modPath);
                break;
            case "music":
                addModMusic(mod, modPath);
                break;
        }
    }

    public void addModMap(JsonElement modMap)
    {
        var viewElem = modMap.GetProperty("view");
        int mapIndex = modMap.GetProperty("level").GetInt32() - 1;

        List<string> view = new();
        foreach (var l in viewElem.EnumerateArray())
        {
            view.Add(l.GetString() ?? "");
        }

        GameMap map = new GameMap(view.ToArray());
        while (mapList.Count <= mapIndex)
        {
            mapList.Add(null!);
        }
        mapList[mapIndex] = map;
    }

    private void addModSprite(JsonElement mod, string modPath)
    {
        var spriteMap = GamePanel.getSpriteMap();
        string type = mod.TryGetProperty("type", out var t) ? t.GetString() ?? "" : "";
        List<string> atributs = new();

        string emoji = mod.TryGetProperty("sprite", out var s) ? s.GetString() ?? "" : "";
        atributs.Add(emoji);

        string colorString = mod.TryGetProperty("color", out var c) ? c.GetString() ?? "" : "";
        atributs.Add(colorString);

        string fileString = mod.TryGetProperty("filename", out var f) ? f.GetString() ?? "" : "";
        string fullSpritePath = Path.Combine(modPath, fileString);
        atributs.Add(fullSpritePath);

        atributs.Add("modded");
        spriteMap[type] = atributs;
        GamePanel.setSpriteMap(spriteMap);
        reloadSprites();
    }

    private void addModMusic(JsonElement mod, string modPath)
    {
        string soundType = mod.GetProperty("type").GetString() ?? "music";
        string soundPath = Path.Combine(modPath, mod.GetProperty("filename").GetString() ?? "");
        var newMap = musicManager.getSoundsMap();
        newMap[soundType] = soundPath;
        musicManager.setSoundsMap(newMap);
        stopMusic();
        playMusic(false);
    }

    private void addModSound(JsonElement mod, string modPath)
    {
        string soundType = mod.GetProperty("type").GetString() ?? "";
        string soundPath = Path.Combine(modPath, mod.GetProperty("filename").GetString() ?? "");
        var newMap = soundFXManager.getSoundsMap();
        newMap[soundType] = soundPath;
        soundFXManager.setSoundsMap(newMap);
    }

    public void playMusic(bool fromResource)
    {
        stopMusic();
        musicManager.playMusic(fromResource);
    }

    public void PlayMusic(bool fromResource) => playMusic(fromResource);

    public int getIceCream() => iceCream;
    public int GetIceCream() => iceCream;

    public int getLives() => lives;
    public int GetLives() => lives;

    private TileType[][] createTileMapForAI()
    {
        TileType[][] tileMap = new TileType[getRows()][];

        for (int row = 0; row < getRows(); row++)
        {
            tileMap[row] = new TileType[getCols()];
            for (int col = 0; col < getCols(); col++)
            {
                tileMap[row][col] = getType(row, col);
            }
        }

        return tileMap;
    }

    public void saveLastPosition()
    {
        if (player != null)
        {
            lastPositions.Add(new PositionHistory(player.getRow(), player.getCol()));
        }
    }

    public void SaveLastPosition() => saveLastPosition();

    public PositionHistory calculatePosition()
    {
        if (lastPositions.Count < 2)
        {
            return new PositionHistory(player.getRow(), player.getCol());
        }

        PositionHistory previous = lastPositions[lastPositions.Count - 2];
        PositionHistory current = lastPositions[lastPositions.Count - 1];

        int deltaRow = current.getX() - previous.getX();
        int deltaCol = current.getY() - previous.getY();

        int nextRow = player.getRow() + deltaRow;
        int nextCol = player.getCol() + deltaCol;

        return new PositionHistory(nextRow, nextCol);
    }

    private PositionHistory getAmbushingTarget()
    {
        PositionHistory aimPosition = calculatePosition();

        int pRow = player.getRow();
        int pCol = player.getCol();

        if (player.getState() == fallingState)
        {
            int targetRow = pRow;

            while (!isOutOfBounds(targetRow, pCol) && shouldDropAt(targetRow, pCol))
            {
                targetRow++;
            }

            return new PositionHistory(targetRow, pCol);
        }

        if (isOutOfBounds(aimPosition.getX(), aimPosition.getY()))
        {
            return new PositionHistory(pRow, pCol);
        }

        return aimPosition;
    }

    private bool shouldDropAt(int row, int col)
    {
        return !isSolid(row + 1, col)
                && !isRail(row, col)
                && !isStair(row, col)
                && !isStair(row + 1, col)
                && !isEnemy(row + 1, col);
    }

    private class EasyNode
    {
        public int row;
        public int col;
        public int distance;

        public EasyNode(int row, int col, int distance)
        {
            this.row = row;
            this.col = col;
            this.distance = distance;
        }
    }

    private PositionHistory? canReachEasy(int enemyRow, int enemyCol, int pRow, int pCol)
    {
        const int MAX_EASY_DISTANCE = 2;

        if (isOutOfBounds(enemyRow, enemyCol) || isOutOfBounds(pRow, pCol))
        {
            return null;
        }

        int manhattanDistance = Math.Abs(enemyRow - pRow) + Math.Abs(enemyCol - pCol);

        if (manhattanDistance > MAX_EASY_DISTANCE)
        {
            return null;
        }

        bool[,] visited = new bool[getRows(), getCols()];
        Queue<EasyNode> queue = new();

        visited[enemyRow, enemyCol] = true;
        queue.Enqueue(new EasyNode(enemyRow, enemyCol, 0));

        while (queue.Count > 0)
        {
            EasyNode current = queue.Dequeue();

            if (current.row == pRow && current.col == pCol)
            {
                return new PositionHistory(pRow, pCol);
            }

            if (current.distance >= MAX_EASY_DISTANCE)
            {
                continue;
            }

            tryAddEasyPosition(queue, visited, current, Direction.UP, pRow, pCol);
            tryAddEasyPosition(queue, visited, current, Direction.DOWN, pRow, pCol);
            tryAddEasyPosition(queue, visited, current, Direction.LEFT, pRow, pCol);
            tryAddEasyPosition(queue, visited, current, Direction.RIGHT, pRow, pCol);
        }

        return null;
    }

    private void tryAddEasyPosition(
            Queue<EasyNode> queue,
            bool[,] visited,
            EasyNode current,
            Direction direction,
            int targetRow,
            int targetCol
    )
    {
        int nextRow = current.row + direction.getDr();
        int nextCol = current.col + direction.getDc();

        if (isOutOfBounds(nextRow, nextCol))
        {
            return;
        }

        if (visited[nextRow, nextCol])
        {
            return;
        }

        if (!canEnemyMoveEasyFromTo(
                current.row,
                current.col,
                nextRow,
                nextCol,
                direction,
                targetRow,
                targetCol
        ))
        {
            return;
        }

        visited[nextRow, nextCol] = true;
        queue.Enqueue(new EasyNode(nextRow, nextCol, current.distance + 1));
    }

    private bool canEnemyMoveEasyFromTo(
            int row,
            int col,
            int nextRow,
            int nextCol,
            Direction direction,
            int targetRow,
            int targetCol
    )
    {
        if (isOutOfBounds(nextRow, nextCol))
        {
            return false;
        }

        if (isSolid(nextRow, nextCol))
        {
            return false;
        }

        if (isEnemy(nextRow, nextCol)
                && !(nextRow == targetRow && nextCol == targetCol))
        {
            return false;
        }

        if (shouldEnemyDrop(row, col))
        {
            return direction == Direction.DOWN;
        }

        if (direction == Direction.UP)
        {
            return isStair(row, col) || isStair(nextRow, nextCol);
        }

        if (direction == Direction.DOWN)
        {
            return isStair(row, col)
                    || isStair(nextRow, nextCol)
                    || !hasGroundBelowForEnemy(row, col);
        }

        if (direction == Direction.LEFT || direction == Direction.RIGHT)
        {
            return hasGroundBelowForEnemy(row, col)
                    || hasGroundBelowForEnemy(nextRow, nextCol)
                    || isRail(row, col)
                    || isRail(nextRow, nextCol)
                    || isStair(row, col)
                    || isStair(nextRow, nextCol);
        }

        return false;
    }

    private bool hasGroundBelowForEnemy(int row, int col)
    {
        int rowBelow = row + 1;

        if (isOutOfBounds(rowBelow, col))
        {
            return true;
        }

        if (isSolid(rowBelow, col))
        {
            return true;
        }

        if (isMolten(rowBelow, col))
        {
            return true;
        }

        return isRail(row, col) || isStair(row, col);
    }

    private void moveIceCreamEnemy(IceCreamEnemy enemy)
    {
        if (!enemy.hasIceCream())
        {
            collectIceCreamForEnemy(enemy);
        }

        if (enemy.hasIceCream())
        {
            moveEnemyAwayFromPlayer(enemy);
            return;
        }

        moveEnemyToNearestIceCream(enemy);
    }

    private void collectIceCreamForEnemy(IceCreamEnemy enemy)
    {
        int row = enemy.getRow();
        int col = enemy.getCol();

        if (getType(row, col) == TileType.ICECREAM)
        {
            blocks[row][col] = new Block(TileType.BLANK);
            enemy.collectIceCream();
        }
    }

    private void moveEnemyToNearestIceCream(IceCreamEnemy enemy)
    {
        Direction? bestDirection = null;
        int bestDistance = int.MaxValue;

        int enemyRow = enemy.getRow();
        int enemyCol = enemy.getCol();

        for (int row = 0; row < getRows(); row++)
        {
            for (int col = 0; col < getCols(); col++)
            {
                if (getType(row, col) != TileType.ICECREAM)
                {
                    continue;
                }

                Direction? direction = buscador.getShortestDirection(
                        createPathMapForAI(enemy),
                        createTileMapForAI(),
                        enemyRow,
                        enemyCol,
                        row,
                        col
                );

                if (direction == null)
                {
                    continue;
                }

                int distance = Math.Abs(enemyRow - row) + Math.Abs(enemyCol - col);

                if (distance < bestDistance)
                {
                    bestDistance = distance;
                    bestDirection = direction;
                }
            }
        }

        if (bestDirection == null)
        {
            return;
        }

        int nextRow = enemy.getRow() + bestDirection.getDr();
        int nextCol = enemy.getCol() + bestDirection.getDc();

        if (isEnemy(nextRow, nextCol))
        {
            return;
        }

        if (canMoveTo(nextRow, nextCol) || isMolten(nextRow, nextCol))
        {
            enemy.setPosition(nextRow, nextCol);
        }

        collectIceCreamForEnemy(enemy);
    }

    private void moveEnemyAwayFromPlayer(IceCreamEnemy enemy)
    {
        Direction? bestDirection = null;

        int enemyRow = enemy.getRow();
        int enemyCol = enemy.getCol();

        int currentDistance = distanceToPlayer(enemyRow, enemyCol);
        int bestDistance = currentDistance;

        foreach (Direction direction in Direction.values())
        {
            int nextRow = enemyRow + direction.getDr();
            int nextCol = enemyCol + direction.getDc();

            if (!canEnemyEscapeTo(enemyRow, enemyCol, nextRow, nextCol, direction))
            {
                continue;
            }

            int distance = distanceToPlayer(nextRow, nextCol);

            if (distance > bestDistance)
            {
                bestDistance = distance;
                bestDirection = direction;
            }
        }

        if (bestDirection == null)
        {
            return;
        }

        int targetRow = enemy.getRow() + bestDirection.getDr();
        int targetCol = enemy.getCol() + bestDirection.getDc();

        enemy.setPosition(targetRow, targetCol);
    }

    private int distanceToPlayer(int row, int col)
    {
        return Math.Abs(row - player.getRow()) + Math.Abs(col - player.getCol());
    }

    private bool canEnemyEscapeTo(
            int row,
            int col,
            int nextRow,
            int nextCol,
            Direction direction
    )
    {
        if (isOutOfBounds(nextRow, nextCol))
        {
            return false;
        }

        if (isSolid(nextRow, nextCol))
        {
            return false;
        }

        if (isEnemy(nextRow, nextCol))
        {
            return false;
        }

        if (direction == Direction.UP)
        {
            return isStair(row, col) || isStair(nextRow, nextCol);
        }

        if (direction == Direction.DOWN)
        {
            return isStair(row, col)
                    || isStair(nextRow, nextCol)
                    || !hasGroundBelowForEnemy(row, col);
        }

        if (direction == Direction.LEFT || direction == Direction.RIGHT)
        {
            return hasGroundBelowForEnemy(row, col)
                    || hasGroundBelowForEnemy(nextRow, nextCol)
                    || isRail(row, col)
                    || isRail(nextRow, nextCol)
                    || isStair(row, col)
                    || isStair(nextRow, nextCol);
        }

        return false;
    }

    private void dropIceCreamIfNeeded(Enemy enemy)
    {
        if (enemy is not IceCreamEnemy iceCreamEnemy)
        {
            return;
        }

        if (!iceCreamEnemy.hasIceCream())
        {
            return;
        }

        int row = iceCreamEnemy.getRow();
        int col = iceCreamEnemy.getCol();

        if (isOutOfBounds(row, col))
        {
            return;
        }

        blocks[row][col] = new Block(TileType.ICECREAM);
        blocks[row][col].setPrintables();

        iceCreamEnemy.dropIceCream();
    }

    private PositionHistory? findEnemyRespawnPosition(Enemy enemy)
    {
        int respawnRow = enemy.getRespawnRow();
        int respawnCol = enemy.getRespawnCol();

        if (canEnemyRespawnAt(respawnRow, respawnCol))
        {
            return new PositionHistory(respawnRow, respawnCol);
        }

        for (int offset = 1; offset < getCols(); offset++)
        {
            int leftCol = respawnCol - offset;
            int rightCol = respawnCol + offset;

            if (canEnemyRespawnAt(respawnRow, leftCol))
            {
                return new PositionHistory(respawnRow, leftCol);
            }

            if (canEnemyRespawnAt(respawnRow, rightCol))
            {
                return new PositionHistory(respawnRow, rightCol);
            }
        }

        return null;
    }

    private bool canEnemyRespawnAt(int row, int col)
    {
        if (isOutOfBounds(row, col))
        {
            return false;
        }

        if (player.getRow() == row && player.getCol() == col)
        {
            return false;
        }

        if (isEnemy(row, col))
        {
            return false;
        }

        if (isSolid(row, col))
        {
            return false;
        }

        if (isFire(row, col))
        {
            return false;
        }

        return true;
    }

    private void moveBasicEnemy(Enemy enemy)
    {
        Direction? direction;

        if (canBasicEnemySeePlayer(enemy))
        {
            direction = getDirectionTowards(
                    enemy.getRow(),
                    enemy.getCol(),
                    player.getRow(),
                    player.getCol()
            );
        }
        else
        {
            direction = getDirectionToEnemyHome(enemy);
        }

        if (direction == null)
        {
            return;
        }

        int nextRow = enemy.getRow() + direction.getDr();
        int nextCol = enemy.getCol() + direction.getDc();

        if (canBasicEnemyMoveTo(enemy.getRow(), enemy.getCol(), nextRow, nextCol, direction))
        {
            enemy.setPosition(nextRow, nextCol);
        }
    }

    private bool canBasicEnemySeePlayer(Enemy enemy)
    {
        int enemyRow = enemy.getRow();
        int enemyCol = enemy.getCol();

        int playerRow = player.getRow();
        int playerCol = player.getCol();

        bool sameRow = enemyRow == playerRow;
        bool sameCol = enemyCol == playerCol;

        if (!sameRow && !sameCol)
        {
            return false;
        }

        int distance = Math.Abs(enemyRow - playerRow) + Math.Abs(enemyCol - playerCol);

        if (distance >= 15)
        {
            return false;
        }

        return hasLineOfSight(enemyRow, enemyCol, playerRow, playerCol);
    }

    private bool hasLineOfSight(int startRow, int startCol, int targetRow, int targetCol)
    {
        if (startRow == targetRow)
        {
            int minCol = Math.Min(startCol, targetCol) + 1;
            int maxCol = Math.Max(startCol, targetCol);

            for (int col = minCol; col < maxCol; col++)
            {
                if (isSolid(startRow, col))
                {
                    return false;
                }
            }

            return true;
        }

        if (startCol == targetCol)
        {
            int minRow = Math.Min(startRow, targetRow) + 1;
            int maxRow = Math.Max(startRow, targetRow);

            for (int row = minRow; row < maxRow; row++)
            {
                if (isSolid(row, startCol))
                {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private Direction? getDirectionTowards(int fromRow, int fromCol, int targetRow, int targetCol)
    {
        if (fromRow < targetRow)
        {
            return Direction.DOWN;
        }

        if (fromRow > targetRow)
        {
            return Direction.UP;
        }

        if (fromCol < targetCol)
        {
            return Direction.RIGHT;
        }

        if (fromCol > targetCol)
        {
            return Direction.LEFT;
        }

        return null;
    }

    private bool canBasicEnemyMoveTo(
            int row,
            int col,
            int nextRow,
            int nextCol,
            Direction direction
    )
    {
        if (isOutOfBounds(nextRow, nextCol))
        {
            return false;
        }

        if (isSolid(nextRow, nextCol))
        {
            return false;
        }

        if (isEnemy(nextRow, nextCol))
        {
            return false;
        }

        if (direction == Direction.UP)
        {
            return isStair(row, col) || isStair(nextRow, nextCol);
        }

        if (direction == Direction.DOWN)
        {
            return isStair(row, col)
                    || isStair(nextRow, nextCol)
                    || canMoveTo(nextRow, nextCol)
                    || isMolten(nextRow, nextCol);
        }

        if (direction == Direction.LEFT || direction == Direction.RIGHT)
        {
            return canMoveTo(nextRow, nextCol)
                    || isMolten(nextRow, nextCol)
                    || isRail(row, col)
                    || isRail(nextRow, nextCol)
                    || isStair(row, col)
                    || isStair(nextRow, nextCol);
        }

        return false;
    }

    private Direction? getDirectionToEnemyHome(Enemy enemy)
    {
        if (enemy.getRow() == enemy.getHomeRow()
                && enemy.getCol() == enemy.getHomeCol())
        {
            return null;
        }

        return buscador.getShortestDirection(
                createPathMapForAI(enemy),
                createTileMapForAI(),
                enemy.getRow(),
                enemy.getCol(),
                enemy.getHomeRow(),
                enemy.getHomeCol()
        );
    }

    public void reloadAudioAfterLoad()
    {
        musicManager = new SoundManager();
        soundFXManager = new SoundManager();

        isMuted = false;

        playMusic(!GamePanel.hasGame());
        changeVolume(gameVolume);
    }

    public void ReloadAudioAfterLoad() => reloadAudioAfterLoad();

    public void stopMusic()
    {
        musicManager?.stopMusic();
    }

    public void StopMusic() => stopMusic();
}

