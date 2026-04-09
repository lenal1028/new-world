package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    public static void main(String[] args) {

        Tessellation tessellation = new Tessellation(3);
        int width = tessellation.getWidth();
        int height = tessellation.getHeight();

        TERenderer ter = new TERenderer();
        ter.initialize(60, 60);
        TETile[][] world = new TETile[60][60];

        for (int x = 0; x < 60; x++) {
            for (int y = 0; y < 60; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        Hexagon test = new Hexagon(new Position(30, 30), 4, Tileset.SAND);
        for (Position position : test.getHexPositions()) {
            world[position.getX()][position.getY()] = test.getTile();
        }

        ter.renderFrame(world);
    }

    private static void addHexagon(Hexagon hexagon, TETile[][] world) {
        for (Position position :hexagon.getHexPositions()) {
            world[position.getX()][position.getY()] = hexagon.getTile();
        }
    }


}
