package byow.lab12;

import byow.Core.RandomUtils;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.*;

public class Tessellation {
    private int side;
    private int width;
    private int height;
    ;

    // A list of the hexagons in this tesselation
    private List<Hexagon> hexagons;

    //s msp og tiles to enable easy random selection of tiles
    private static Map<Integer, TETile> tiles = Map.of(
            0, Tileset.FLOWER,
            1, Tileset.WALL,
            2, Tileset.AVATAR,
            3, Tileset.FLOOR,
            4, Tileset.GRASS,
            5, Tileset.LOCKED_DOOR,
            6, Tileset.MOUNTAIN,
            7, Tileset.SAND,
            8, Tileset.TREE,
            9, Tileset.UNLOCKED_DOOR
    );

    public Tessellation(int side) {
        this.side = side;
        this.hexagons = new ArrayList<>();

        //Map each column number to its upper left starting position
        // We use dummy hexagons to compute the correct starting position
        Map<Integer, Position> statingPosition = new HashMap<>();
        Hexagon dummy = new Hexagon(new Position(0,0), this.side, null);
        width = dummy.getRowWidth(this.side) * 3 + this.side * 2;
        height = 2 * side * 5;
        int middle = width /2 ;
        statingPosition.put(2, new Position(middle, height));

        Hexagon dummy2 = new Hexagon(new Position(middle,height), this.side, null);
        Position position1 = new Position(dummy2.getLowerLeft(), -this.side, -1);
        statingPosition.put(1, position1);

        Position position3 = new Position(dummy2.getLowerRight(), 1, -1);
        statingPosition.put(3, position3);

        Hexagon dummy1 = new Hexagon(position1, this.side, null);
        Position position0 = new Position(dummy1.getLowerLeft(), -this.side, -1);
        statingPosition.put(0, position0);

        Hexagon dummy3 = new Hexagon(position3, this.side, null);
        Position position4 = new Position(dummy3.getLowerRight(),1, -1);
        statingPosition.put(4, position4);

//        for (int i =0; i <5; i++) {
//            this.addColumn(statingPosition.get(i), COLUMN_SIZES.get(i), dummy.getHexHeight());
//        }
    }

        //Create Hexagon that make up a column of NUM hexagons, each of height HEIGHT,
        // the top hexagon upper left corner is at STARTING POSITION

    private void addColumn(Position startingPosition, int num, int height) {
        for (int i = 0; i < num; i++) {
            int y = startingPosition.getY() - i * height;
            Position position = new Position(startingPosition.getX(), y);
            TETile tile = tiles.get(RandomUtils.uniform(new Random(), 20));
            this.hexagons.add(new Hexagon(position, this.side, tile));
        }
    }

    public int getWidth(){
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}