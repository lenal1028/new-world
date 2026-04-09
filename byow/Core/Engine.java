package byow.Core;

import byow.InputDemo.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.lab12.Position;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import static byow.TileEngine.Tileset.*;

public class Engine implements Serializable {
    private static final TETile EATEN_FLOOR = new TETile(' ', new Color(128, 192, 128),
            Color.black, "eaten floor");
    private static final TETile ENEMY_TILE = new TETile('\u2620', Color.red, Color.black, "enemy");
    private static final long GAME_DURATION_MS = 120_000L;
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 40;
    private World newWorld;
    private TETile[][] finalWorldFrame;
    private TETile[][] startWorld; /** for replay*/
    private Position position = null;
    private ArrayList<Character> movements = new ArrayList<>();
    private String name = "";
    private StringInputDevice inputSource;
    private int lastMouseX = -1;
    private int lastMouseY = -1;
    private int score = 0;
    private int floorsCleared = 0;
    private long gameStartTimeMs = 0L;
    private Position enemyPosition = null;
    private TETile enemyUnderlyingTile = Tileset.FLOOR;

    /**
     * Method used for exploring a fresh world.
     * This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        mainMenu();
    }

    private void useMenuScale() {
        StdDraw.setXscale(0, 1);
        StdDraw.setYscale(0, 1);
    }

    private void mainMenu() {
        useMenuScale();
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 40));
        StdDraw.text(.5, .7, "CS61B: THE GAME");
        StdDraw.text(.5, .4, "New Game (N)");
        StdDraw.text(.5, .3, "Load Game (L)");
        StdDraw.text(.5, .2, "Quit (Q)");
        StdDraw.show();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = Character.toLowerCase(StdDraw.nextKeyTyped());
                menuInput(key);
            }
        }
    }

    private void menuInput(Character key) {
        if (key == 'n') {
            newGame();
        } else if (key == 'l') {
            /** load movement set and last world from the last saved */

            TETile[][] loadW = loadWorld();
            if (loadW == null) {
                StdDraw.clear(StdDraw.BLACK);
                StdDraw.setPenColor(StdDraw.WHITE);
                StdDraw.setFont(new Font("Arial", Font.BOLD, 40));
                StdDraw.text(.5, .7, "No file to load");
                StdDraw.pause(2000);
                mainMenu();
            } else {
                movements = loadMovements();
                finalWorldFrame = loadW;
                name = loadName();
                ter.initialize(WIDTH, HEIGHT);
                ter.renderFrame(finalWorldFrame);
                rePosition(finalWorldFrame);
                replay(false);
                startWorld = getCopyWorld(finalWorldFrame);
                play();
            }
        }
    }

    private void avatar() {
        useMenuScale();
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 20));
        drawAvatarPrompt();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                if (key == '1' || key == StdDraw.START_SIGNAL || key == '\n' || key == '\r') {
                    break;
                }
                if (key == '\b' || key == 127) {
                    if (!name.isEmpty()) {
                        name = name.substring(0, name.length() - 1);
                        drawAvatarPrompt();
                    }
                    continue;
                }
                if (!Character.isISOControl(key)) {
                    name += key;
                    drawAvatarPrompt();
                }
            }
        }
    }

    private void newGame() {
        score = 0;
        floorsCleared = 0;
        gameStartTimeMs = System.currentTimeMillis();
        String seedstring = "";
        long seed;
        drawSeedPrompt(seedstring);

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char secondKey = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (secondKey == '\b' || secondKey == 127) {
                    if (!seedstring.isEmpty()) {
                        seedstring = seedstring.substring(0, seedstring.length() - 1);
                    }
                    drawSeedPrompt(seedstring);
                } else if (Character.isDigit(secondKey)) {
                    seedstring += secondKey;
                    drawSeedPrompt(seedstring);
                } else if (secondKey == 's' && !seedstring.isEmpty()) {
                    seed = Long.parseLong(seedstring);
                    break;
                }
            }
        }
        newWorld = new World(seed);
        avatar();
        loadWorldState(newWorld, true);
        play();
    }

    private void drawAvatarPrompt() {
        useMenuScale();
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 20));
        StdDraw.text(.5, .65, "Enter a name for your avatar followed by \"1\"");
        StdDraw.text(.5, .58, "or hit \"Enter\" to start");
        StdDraw.text(.5, .5, name);
        StdDraw.show();
    }

    private void drawSeedPrompt(String seedstring) {
        useMenuScale();
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 20));
        StdDraw.text(.5, .55, "Enter seed followed by 's'");
        StdDraw.text(.5, .4, seedstring);
        StdDraw.show();
    }

    private void play() {
        if (position == null) {
            position = newWorld.avatarP;
        }
        while (true) {
            if (isTimeUp()) {
                gameOver("Time's up!");
                return;
            }
            tileName();
            if (StdDraw.hasNextKeyTyped()) {
                char key = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (key == ':') {
                    while (true) {
                        if (StdDraw.hasNextKeyTyped()) {
                            key = StdDraw.nextKeyTyped();
                            if (key == 'q') {
                                saveName(name);
                                saveWorld(startWorld);
                                saveMovement(movements);
                                System.exit(0);
                            }
                        }
                    }
                } else {
                    /** move regularly and save move to movement set */
                    position = move(position, key, true);
                    if (position == null) {
                        return;
                    }
                    if (position != null && enemyPosition != null && position.equals(enemyPosition)) {
                        gameOver("The enemy caught you!");
                        return;
                    }
                }
            }
        }
    }

    private Position move(Position position1, Character key, boolean notReplay) {
        /**
         Update to movement set if it is NOT a replay-movement
         */
        if (notReplay) {
            movements.add(key);
        }

        int x = position1.getX();
        int y = position1.getY();
        Position newP = position1;
        TETile newMove;
        boolean reachedDoor = false;
        boolean hitEnemy = false;
        /** Move up*/
        if (key == 'w') {
            newMove = finalWorldFrame[x][y + 1];
            if (isWalkableFloor(newMove)) {
                finalWorldFrame[x][y + 1] = Tileset.AVATAR;
                newP = new Position(x, y + 1);
            } else if (newMove.description().equals("enemy")) {
                hitEnemy = true;
            } else if (newMove.description().equals("locked door")) {
                reachedDoor = true;
            }
        } else if (key == 's') /** move down*/ {
            newMove = finalWorldFrame[x][y - 1];
            if (isWalkableFloor(newMove)) {
                finalWorldFrame[x][y - 1] = Tileset.AVATAR;
                newP = new Position(x, y - 1);
            } else if (newMove.description().equals("enemy")) {
                hitEnemy = true;
            } else if (newMove.description().equals("locked door")) {
                reachedDoor = true;
            }
        } else if (key == 'a') { /** Move left*/
            newMove = finalWorldFrame[x - 1][y];
            if (isWalkableFloor(newMove)) {
                finalWorldFrame[x - 1][y] = Tileset.AVATAR;
                newP = new Position(x - 1, y);
            } else if (newMove.description().equals("enemy")) {
                hitEnemy = true;
            } else if (newMove.description().equals("locked door")) {
                reachedDoor = true;
            }
        } else { /** Move right*/
            newMove = finalWorldFrame[x + 1][y];
            if (isWalkableFloor(newMove)) {
                finalWorldFrame[x + 1][y] = AVATAR;
                newP = new Position(x + 1, y);
            } else if (newMove.description().equals("enemy")) {
                hitEnemy = true;
            } else if (newMove.description().equals("locked door")) {
                reachedDoor = true;
            }
        }
        if (hitEnemy) {
            gameOver("The enemy caught you!");
            return null;
        }
        if (reachedDoor) {
            advanceFloor();
            return position;
        }
        if (isWalkableFloor(newMove)) {
            if (newMove == Tileset.FLOOR) {
                score += 1;
            }
            finalWorldFrame[x][y] = EATEN_FLOOR;
            position = newP;
            moveEnemy();
            if (enemyPosition != null && enemyPosition.equals(newP)) {
                gameOver("The enemy caught you!");
                return null;
            }
            ter.renderFrame(finalWorldFrame);
        }
        return newP;
    }

    private void advanceFloor() {
        long nextSeed = System.nanoTime();
        floorsCleared += 1;
        loadWorldState(new World(nextSeed), false);
    }


    //@source roommate who previously took 61b w/ hug
    private void saveWorld(TETile[][] world) {
        File f = new File("./savefile.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fo = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fo);
            os.writeObject(world);
            //os.writeObject(finalWorldFrame);
            os.close();
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    //@source roommate who previously took 61b w/ hug
    private TETile[][] loadWorld() {
        File file = new File("./savefile.txt");
        if (file.exists()) {
            try {
                FileInputStream f = new FileInputStream(file);
                ObjectInputStream o = new ObjectInputStream(f);
                finalWorldFrame = (TETile[][]) o.readObject();
                startWorld = getCopyWorld(finalWorldFrame);
                o.close();
                return finalWorldFrame;
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println("input/output initializing failed");
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                e.printStackTrace();
            }
        }
        return null;
    }

    private void rePosition(TETile[][] finalWorldFrame1) {
        for (int w = 0; w < WIDTH - 1; w++) {
            for (int h = 0; h < HEIGHT - 1; h++) {
                if (finalWorldFrame1[w][h].description().equals("you")) {
                    position = new Position(w, h);
                }
            }
        }
    }

    private void replay(boolean inputString) {
        int noOfMovement = movements.size();
        for (int i = 0; i < noOfMovement; i++) {
            Character key = movements.get(i);
            position = move(position, key, false);
            if (!inputString) {
                StdDraw.pause(100);
            }
        }
        movements = new ArrayList<>();
    }


    private void saveMovement(ArrayList<Character> movements1) {
        File f = new File("./savemovements.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fo = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fo);
            os.writeObject(movements1);
            os.close();
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }


    private ArrayList<Character> loadMovements() {
        ArrayList<Character> newMovement = new ArrayList<>();
        File fileM = new File("./savemovements.txt");
        if (fileM.exists()) {
            try {
                FileInputStream f = new FileInputStream(fileM);
                ObjectInputStream o = new ObjectInputStream(f);
                ArrayList readMovement = (ArrayList) o.readObject();
                o.close();
                f.close();
                for (Object movement : readMovement) {
                    newMovement.add(movement.toString().charAt(0));
                }
                return newMovement;
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println("input/output initializing failed");
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                e.printStackTrace();
            }
        }
        return null;
    }

    private void saveName(String name1) {
        File f = new File("./savename.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fo = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fo);
            os.writeObject(name1);
            os.close();
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }


    private String loadName() {
        File file = new File("./savename.txt");
        if (file.exists()) {
            try {
                FileInputStream f = new FileInputStream(file);
                ObjectInputStream o = new ObjectInputStream(f);
                String readName = (String) o.readObject();
                o.close();
                return readName;
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println("input/output initializing failed");
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                e.printStackTrace();
            }
        }
        return null;
    }


    public TETile[][] getCopyWorld(TETile[][] world) {
        TETile[][] copyWorld = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            copyWorld[x] = world[x].clone();
        }
        return copyWorld;
    }


    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        inputSource = new StringInputDevice(input);
        char choice = inputSource.getNextKey();
        if (Character.toLowerCase(choice) == 'n') {
            long seed = 0;
            while (inputSource.possibleNextInput()) {
                choice = inputSource.getNextKey();
                if (Character.toLowerCase(choice) != 's') {
                    seed = seed * 10 + Long.parseLong(String.valueOf(choice));
                } else {
                    newWorld = new World(seed);
                    position = newWorld.avatarP;
                    finalWorldFrame = newWorld.getWorld();
                    while (inputSource.possibleNextInput()) {
                        char key = inputSource.getNextKey();
                        if (key == ':' && inputSource.possibleNextInput()
                                && inputSource.getNextKey() == 'q') {
                            saveWorld(finalWorldFrame);
                            break;
                        } else {
                            position = moveinputString(position, key, true);
                        }
                    }
                }
            }
        } else {
            finalWorldFrame = loadWorld();
            rePosition(finalWorldFrame);
            playInputString();
        }
        return finalWorldFrame;
    }

    private void playInputString() {
        if (position == null) {
            position = newWorld.avatarP;
        }
        while (true) {
            if (inputSource.possibleNextInput()) {
                char key = Character.toLowerCase(inputSource.getNextKey());
                if (key == ':') {
                    while (true) {
                        if (inputSource.possibleNextInput()) {
                            key = inputSource.getNextKey();
                            if (key == 'q') {
                                saveWorld(finalWorldFrame);
                                saveMovement(movements);
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                } else {
                    /** move regularly and save move to movement set */
                    position = moveinputString(position, key, true);
                }
            } else {
                break;
            }
        }
    }

    private Position moveinputString(Position position1, Character key, boolean notReplay) {
        /**
         Update to movement set if it is NOT a replay-movement
         */
        if (notReplay) {
            movements.add(key);
        }

        int x = position1.getX();
        int y = position1.getY();
        Position newP = position1;
        TETile newMove;
        boolean reachedDoor = false;
        /** Move up*/
        if (key == 'w') {
            newMove = finalWorldFrame[x][y + 1];
            if (isWalkableFloor(newMove)) {
                finalWorldFrame[x][y + 1] = Tileset.AVATAR;
                newP = new Position(x, y + 1);
            } else if (newMove.description().equals("locked door")) {
                reachedDoor = true;
            }
        } else if (key == 's') /** move down*/ {
            newMove = finalWorldFrame[x][y - 1];
            if (isWalkableFloor(newMove)) {
                finalWorldFrame[x][y - 1] = Tileset.AVATAR;
                newP = new Position(x, y - 1);
            } else if (newMove.description().equals("locked door")) {
                reachedDoor = true;
            }
        } else if (key == 'a') { /** Move left*/
            newMove = finalWorldFrame[x - 1][y];
            if (isWalkableFloor(newMove)) {
                finalWorldFrame[x - 1][y] = Tileset.AVATAR;
                newP = new Position(x - 1, y);
            } else if (newMove.description().equals("locked door")) {
                reachedDoor = true;
            }
        } else { /** Move right*/
            newMove = finalWorldFrame[x + 1][y];
            if (isWalkableFloor(newMove)) {
                finalWorldFrame[x + 1][y] = AVATAR;
                newP = new Position(x + 1, y);
            } else if (newMove.description().equals("locked door")) {
                reachedDoor = true;
            }
        }
        if (reachedDoor) {
            floorsCleared += 1;
            loadWorldState(new World(System.nanoTime()), false);
            return position;
        }
        if (isWalkableFloor(newMove)) {
            if (newMove == Tileset.FLOOR) {
                score += 1;
            }
            finalWorldFrame[x][y] = EATEN_FLOOR;
        }
        return newP;
    }

    private boolean isWalkableFloor(TETile tile) {
        return tile == Tileset.FLOOR || tile == EATEN_FLOOR;
    }

    private void loadWorldState(World world, boolean initializeRenderer) {
        newWorld = world;
        finalWorldFrame = newWorld.getWorld();
        startWorld = getCopyWorld(finalWorldFrame);
        position = newWorld.avatarP;
        lastMouseX = -1;
        lastMouseY = -1;
        placeEnemy();
        if (initializeRenderer) {
            ter.initialize(newWorld.WIDTH, newWorld.HEIGHT);
        }
        ter.renderFrame(finalWorldFrame);
    }

    private void placeEnemy() {
        enemyPosition = null;
        enemyUnderlyingTile = Tileset.FLOOR;
        for (int x = WIDTH - 2; x >= 1; x--) {
            for (int y = 1; y < HEIGHT - 1; y++) {
                TETile tile = finalWorldFrame[x][y];
                if (tile == Tileset.FLOOR && !new Position(x, y).equals(position)) {
                    enemyPosition = new Position(x, y);
                    enemyUnderlyingTile = tile;
                    finalWorldFrame[x][y] = ENEMY_TILE;
                    return;
                }
            }
        }
    }

    private void moveEnemy() {
        if (enemyPosition == null) {
            return;
        }
        Position next = nextEnemyStep();
        if (next == null || next.equals(enemyPosition)) {
            return;
        }
        finalWorldFrame[enemyPosition.getX()][enemyPosition.getY()] = enemyUnderlyingTile;
        enemyUnderlyingTile = finalWorldFrame[next.getX()][next.getY()];
        enemyPosition = next;
        finalWorldFrame[enemyPosition.getX()][enemyPosition.getY()] = ENEMY_TILE;
    }

    private Position nextEnemyStep() {
        boolean[][] visited = new boolean[WIDTH][HEIGHT];
        Position[][] previous = new Position[WIDTH][HEIGHT];
        Queue<Position> queue = new ArrayDeque<>();
        queue.add(enemyPosition);
        visited[enemyPosition.getX()][enemyPosition.getY()] = true;

        while (!queue.isEmpty()) {
            Position current = queue.remove();
            if (current.equals(position)) {
                break;
            }
            for (Position neighbor : neighbors(current)) {
                int nx = neighbor.getX();
                int ny = neighbor.getY();
                if (visited[nx][ny]) {
                    continue;
                }
                TETile tile = finalWorldFrame[nx][ny];
                if (!canEnemyEnter(tile, neighbor)) {
                    continue;
                }
                visited[nx][ny] = true;
                previous[nx][ny] = current;
                queue.add(neighbor);
            }
        }

        if (!visited[position.getX()][position.getY()]) {
            return enemyPosition;
        }

        Position step = position;
        while (previous[step.getX()][step.getY()] != null
                && !previous[step.getX()][step.getY()].equals(enemyPosition)) {
            step = previous[step.getX()][step.getY()];
        }
        return step;
    }

    private Position[] neighbors(Position current) {
        ArrayList<Position> result = new ArrayList<>();
        int[][] directions = {
                {0, 1}, {1, 0}, {0, -1}, {-1, 0}
        };
        for (int[] direction : directions) {
            int nx = current.getX() + direction[0];
            int ny = current.getY() + direction[1];
            if (nx >= 0 && ny >= 0 && nx < WIDTH && ny < HEIGHT) {
                result.add(new Position(nx, ny));
            }
        }
        return result.toArray(new Position[0]);
    }

    private boolean canEnemyEnter(TETile tile, Position candidate) {
        return candidate.equals(position) || tile == Tileset.FLOOR || tile == EATEN_FLOOR;
    }

    private boolean isTimeUp() {
        return remainingTimeMs() <= 0;
    }

    private long remainingTimeMs() {
        return Math.max(0L, GAME_DURATION_MS - (System.currentTimeMillis() - gameStartTimeMs));
    }

    private String timerLabel() {
        long remainingSeconds = remainingTimeMs() / 1000;
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void gameOver(String message) {
        enemyPosition = null;
        useMenuScale();
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 30));
        StdDraw.text(.5, .68, message);
        StdDraw.setFont(new Font("Arial", Font.PLAIN, 22));
        StdDraw.text(.5, .56, "Player Name: " + name);
        StdDraw.text(.5, .49, "Final Score: " + score);
        StdDraw.text(.5, .42, "Floors Cleared: " + floorsCleared);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 22));
        StdDraw.text(.5, .28, "Play Again (P)");
        StdDraw.text(.5, .20, "Quit (Q)");
        StdDraw.show();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (key == 'p') {
                    resetRunState();
                    mainMenu();
                    return;
                } else if (key == 'q') {
                    System.exit(0);
                }
            }
        }
    }

    private void resetRunState() {
        newWorld = null;
        finalWorldFrame = null;
        startWorld = null;
        position = null;
        movements = new ArrayList<>();
        name = "";
        score = 0;
        floorsCleared = 0;
        lastMouseX = -1;
        lastMouseY = -1;
        enemyPosition = null;
        enemyUnderlyingTile = Tileset.FLOOR;
        gameStartTimeMs = 0L;
    }


    private void tileName() {
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        if (x >= 0 && y >= 0 && x < WIDTH && y < HEIGHT
                && (x != lastMouseX || y != lastMouseY)) {
            lastMouseX = x;
            lastMouseY = y;
            ter.renderFrame(finalWorldFrame);
            StdDraw.setPenColor(Color.WHITE);
            if (finalWorldFrame[x][y].description().equals("locked door")) {
                StdDraw.text(30, HEIGHT - 1, "locked door");
            } else if (finalWorldFrame[x][y].description().equals("wall")) {
                StdDraw.text(30, HEIGHT - 1, "wall");
            } else if (finalWorldFrame[x][y].description().equals("floor")) {
                StdDraw.text(30, HEIGHT - 1, "floor");
            } else if (finalWorldFrame[x][y].description().equals("eaten floor")) {
                StdDraw.text(30, HEIGHT - 1, "floor");
            } else if (finalWorldFrame[x][y].description().equals("you")) {
                StdDraw.text(30, HEIGHT - 1, "this is you, " + name);
            } else if (finalWorldFrame[x][y].description().equals("enemy")) {
                StdDraw.text(30, HEIGHT - 1, "enemy");
            } else if (finalWorldFrame[x][y].description().equals("nothing")) {
                StdDraw.text(30, HEIGHT - 1, "nothing");
            }
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(4, HEIGHT - 1, "name: " + name);
            StdDraw.text(12, HEIGHT - 1, "score: " + score);
            StdDraw.text(20, HEIGHT - 1, "time: " + timerLabel());
            StdDraw.show();
        }
    }

    public static void main(String[] args) {
        Engine engine = new Engine();
        //engine.interactWithInputString("n123swww:q");
        //engine.interactWithInputString("l");
//      engine.interactWithInputString("n123sss:q");
//      engine.interactWithInputString("lww");
//      engine.interactWithInputString("n123sssww");
        engine.interactWithKeyboard();
    }
}
