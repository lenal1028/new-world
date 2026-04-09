package byow.Core;


import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.lab12.Position;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class World implements Serializable {
    int WIDTH = 80;
    int HEIGHT = 40;
    double MAX_AREA_R = 0.15;
    long seed;
    Random rand;
    TETile[][] world;
    Position avatarP;
    Position lockedDoor;

    public World(long seed) {
        this.seed = seed;
        this.rand = new Random(seed);
        int noRoom = 0;
        List<Room> listRoom = new ArrayList<>();

        world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        int roomArea = 0;
        addRoom(listRoom, roomArea, noRoom);
        addHallways(listRoom);
        List<Room> walls = addWalls(listRoom);
        fillTile(walls, Tileset.WALL);
        fillTile(listRoom, Tileset.FLOOR);
        avatarP = addAvatar(listRoom.get(1));
        lockedDoor = addLockedDoor(listRoom);
    }

    private void addRoom(List<Room> listRoom, int roomArea, int noRoom) {

        while (roomArea <= MAX_AREA_R * WIDTH * HEIGHT) {
            int x = rand.nextInt(WIDTH - 2) + 1;
            int y = rand.nextInt(HEIGHT - 2) + 1;
            int roomH = rand.nextInt(WIDTH / 7) + 3;
            int roomW = rand.nextInt(HEIGHT / 7) + 3;

            Position startingPosition = new Position(x, y);
            Room newRoom = new Room(startingPosition, roomH, roomW);
            boolean intersect = false;

            if (!checkValid(newRoom)) {
                continue;
            }
            for (Room room : listRoom) {
                intersect = newRoom.intersect(room);
                if (intersect) {
                    break;
                }
            }
            if (!intersect) {
                listRoom.add(newRoom);
                noRoom += 1;
                roomArea += newRoom.area;
            }
        }
    }

    public boolean checkValid(Room room) {
        if (room.upR.getX() > WIDTH - 3 || room.upR.getY() > HEIGHT - 3
                || room.downL.getX() < 3 || room.downL.getY() < 3) {
            return false;
        }
        return true;
    }


    private void addHallways(List<Room> listRoom) {
        int sizelistRoom = listRoom.size();
        for (int i = 0; i < sizelistRoom - 1; i++) {
            Room[] hallway = connect(listRoom.get(i), listRoom.get(i + 1));
            listRoom.add(hallway[0]);
            listRoom.add(hallway[1]);
        }
//            Room[] hallway = connect(listRoom.get(7), listRoom.get(8));
//            listRoom.add(hallway[0]);
//            listRoom.add(hallway[1]);
    }


    private Room[] connect(Room roomA, Room roomB) {
        Room[] hallways = new Room[2];
        int xOffsetA = rand.nextInt(roomA.width) + 1;
        int yOffsetA = rand.nextInt(roomA.height) + 1;
        Position posA = new Position(roomA.downL, xOffsetA, yOffsetA);

        int xOffsetB = rand.nextInt(roomB.width) + 1;
        int yOffsetB = rand.nextInt(roomB.height) + 1;
        Position posB = new Position(roomB.downL, xOffsetB, yOffsetB);

        if (posA.getX() <= posB.getX()) {
            hallways[0] = hHallways(posA, posB);
            if (posA.getY() >= posB.getY()) {
                hallways[1] = vHallways(hallways[0].upR, posB);
            } else {
                hallways[1] = vHallways(posB, posA);
            }
        } else {
            Position hPosition = new Position(posB.getX(), posA.getY());
            hallways[0] = hHallways(hPosition, posA);
            if (posA.getY() >= posB.getY()) {
                hallways[1] = vHallways(hallways[0].upL, posB);
            } else {
                hallways[1] = vHallways(posB, posA);
            }
        }
        return hallways;
    }

    private Room hHallways(Position posA, Position posB) {
        return new Room(posA, 1, posB.getX() - posA.getX());
    }

    private Room vHallways(Position posA, Position posB) {
        return new Room(posA, posA.getY() - posB.getY() + 1, 1);
    }

    private List<Room> addWalls(List<Room> listRoom) {
        List<Room> walls = new ArrayList<>();
        for (Room room : listRoom) {
            Position upLwall = new Position(room.upL.getX() - 1, room.upL.getY() + 1);
            Position upRwall = new Position(room.upR.getX() + 1, room.upR.getY() + 1);
            Position downLwall = new Position(room.downL.getX() - 1, room.downL.getY() - 1);
            Room wallRoom = new Room(upLwall, upLwall.getY() - downLwall.getY(),
                    upRwall.getX() - upLwall.getX());
            walls.add(wallRoom);
        }
        return  walls;
    }

    private void fillTile(List<Room> listRoom, TETile tile) {
        for (Room room : listRoom) {
            for (int x = room.downL.getX(); x < room.upR.getX(); x++) {
                for (int y = room.downL.getY(); y < room.upR.getY(); y++) {
                    world[x][y] = tile;
                }
            }
        }

    }

    private Position addAvatar(Room room) {
        int aX = room.upL.getX() + room.width / 2;
        int aY = room.upL.getY() - room.height / 2;
        world[aX][aY] = Tileset.AVATAR;
        return new Position(aX, aY);
    }

    private Position addLockedDoor(List<Room> listRoom) {
        int lrX = 0;
        int lrY = 0;
        int noRoom = listRoom.size();
        for (int i = 0; i < noRoom; i++) {
            Room room = listRoom.get(i);
            lrX = room.downL.getX() + room.width / 2;
            lrY = room.downL.getY() - 1;
            if (world[lrX][lrY].equals(Tileset.WALL)) {
                world[lrX][lrY] = Tileset.LOCKED_DOOR;
                break;
            }
        }
        return new Position(lrX, lrY);
    }


    public TETile[][] getWorld() {
        return world;
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        World newWorld = new World(165434325);
        TETile[][] a = newWorld.getWorld();
        TETile[][] c = a;
//        ter.initialize(newWorld.WIDTH, newWorld.HEIGHT);
//        ter.renderFrame(newWorld.getWorld());
    }
}
