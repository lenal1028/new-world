package byow.Core;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.Font;

/**
 * Minimal window test to verify the local StdDraw implementation opens visibly.
 */
public class WindowSmokeTest {
    public static void main(String[] args) {
        StdDraw.setCanvasSize(800, 400);
        StdDraw.setXscale(0, 1);
        StdDraw.setYscale(0, 1);
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 32));
        StdDraw.text(0.5, 0.6, "BYOW Window Test");
        StdDraw.text(0.5, 0.4, "If you can read this, the window works.");
        StdDraw.show();
        while (true) {
            StdDraw.pause(100);
        }
    }
}
