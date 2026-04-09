package byow.Core;

import edu.princeton.cs.introcs.StdDraw;

/**
 * Simple launcher that initializes the drawing window before entering the game loop.
 */
public class Launcher {
    public static void main(String[] args) {
        StdDraw.setCanvasSize(1280, 640);
        StdDraw.setXscale(0, 1);
        StdDraw.setYscale(0, 1);
        Engine engine = new Engine();
        engine.interactWithKeyboard();
    }
}
