package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.List;

public class Hexagon {

    private Position upperLeft;
    private Position lowerLeft;
    private Position lowerRight;
    private int side;
    private TETile tile;

    public Hexagon(Position upperLeft, int side, TETile tile) {
        this.upperLeft = upperLeft;
        this.side = side;
        this.tile = tile;
        this.lowerLeft = new Position(this.upperLeft, -(this.side - 1), -(this.side - 1));
        this.lowerRight = new Position(this.lowerLeft, this.getRowWidth(this.side) - 1, 0);
    }

    public TETile getTile() {
        return tile;
    }

    public List<Position> getHexPositions() {
        List<Position> positions = new ArrayList<>();
        int height = this.side * 2;
        for (int y = 0; y < height; y++) {
            int startRow = this.getRowWidth(y);
            int width = this.getRowWidth(y);
            for (int x = 0; x < width; x++) {
                positions.add(new Position(startRow + x, this.upperLeft.getY() - y));
            }
        }
        return positions;
    }


    public int getRowWidth(int row) {
        if (row < this.side) {
            return this.side + row*2;
        } else {
            return this.side + (this.side - 1) * 2 - (row % this.side) * 2;
        }
    }


//    public int getHexHeight() {
//        return upperLeft - lowerLeft;
//    }

    public Position getLowerLeft() {
        return lowerLeft;
    }
    public Position getLowerRight() {
        return lowerRight;
    }


    public static void main (String[] args) {
        TERenderer ter = new TERenderer();
        int WIDTH = 60;
        int HEIGHT = 30;
        ter.initialize(WIDTH, HEIGHT);

        // initialize tiles
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        // fills in a block 14 tiles wide by 4 tiles tall
//        for (int x = 20; x < 35; x += 1) {
//            for (int y = 5; y <15; y += 1) {
//                world[x][y] = Tileset.WALL;
//            }
//        }

        Hexagon a = new Hexagon(new Position(5,7), 4, Tileset.SAND);
        for (Position position : a.getHexPositions()) {
            world[position.getX()][position.getY()] = a.getTile();
        }

        ter.renderFrame(world);

    }
}
