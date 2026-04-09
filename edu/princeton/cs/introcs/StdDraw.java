package edu.princeton.cs.introcs;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A lightweight subset of Princeton's StdDraw API used by this BYOW project.
 * It supports the drawing, keyboard, and mouse methods referenced in the codebase.
 */
public final class StdDraw {
    public static final Color BLACK = Color.BLACK;
    public static final Color WHITE = Color.WHITE;
    public static final char START_SIGNAL = '\u0001';

    private static final Object LOCK = new Object();
    private static final int DEFAULT_SIZE = 512;

    private static int width = DEFAULT_SIZE;
    private static int height = DEFAULT_SIZE;
    private static double xmin = 0.0;
    private static double xmax = 1.0;
    private static double ymin = 0.0;
    private static double ymax = 1.0;
    private static Color penColor = BLACK;
    private static Font font = new Font("SansSerif", Font.PLAIN, 16);
    private static boolean defer = false;

    private static BufferedImage offscreenImage;
    private static Graphics2D offscreen;
    private static DrawPanel panel;
    private static JFrame frame;
    private static boolean keyboardHookInstalled = false;

    private static final Deque<Character> keysTyped = new ArrayDeque<>();
    private static double mouseX = 0.0;
    private static double mouseY = 0.0;

    private StdDraw() {
    }

    static {
        init();
    }

    private static void init() {
        synchronized (LOCK) {
            if (offscreenImage == null) {
                initCanvas();
            }
        }
    }

    private static void initCanvas() {
        offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        offscreen = offscreenImage.createGraphics();
        offscreen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        offscreen.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        offscreen.setBackground(WHITE);
        offscreen.setColor(penColor);
        offscreen.setFont(font);
        clear(WHITE);

        if (panel == null) {
            panel = new DrawPanel();
            panel.setPreferredSize(new Dimension(width, height));
            panel.setFocusable(true);
            panel.setFocusTraversalKeysEnabled(false);
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    updateMouse(e);
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    updateMouse(e);
                }

                private void updateMouse(MouseEvent e) {
                    synchronized (LOCK) {
                        mouseX = scaleX(e.getX());
                        mouseY = scaleY(e.getY());
                    }
                }
            };
            panel.addMouseMotionListener(mouseAdapter);
            panel.addMouseListener(mouseAdapter);
        }

        if (!keyboardHookInstalled) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
                    new KeyEventDispatcher() {
                        @Override
                        public boolean dispatchKeyEvent(KeyEvent e) {
                            if (e.getID() == KeyEvent.KEY_TYPED) {
                                synchronized (LOCK) {
                                    keysTyped.addLast(e.getKeyChar());
                                }
                            } else if (e.getID() == KeyEvent.KEY_PRESSED) {
                                char mapped = mapSpecialKey(e);
                                if (mapped != 0) {
                                    synchronized (LOCK) {
                                        keysTyped.addLast(mapped);
                                    }
                                }
                            }
                            return false;
                        }
                    }
            );
            keyboardHookInstalled = true;
        }

        panel.setPreferredSize(new Dimension(width, height));

        if (frame == null) {
            frame = new JFrame("StdDraw");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
            frame.toFront();
            frame.requestFocus();
            frame.setState(JFrame.NORMAL);
            panel.requestFocusInWindow();
        } else {
            frame.setContentPane(panel);
            frame.pack();
            frame.setVisible(true);
            frame.setState(JFrame.NORMAL);
            panel.requestFocusInWindow();
        }
    }

    public static void setCanvasSize(int canvasWidth, int canvasHeight) {
        synchronized (LOCK) {
            if (canvasWidth <= 0 || canvasHeight <= 0) {
                throw new IllegalArgumentException("Canvas dimensions must be positive.");
            }
            width = canvasWidth;
            height = canvasHeight;
            initCanvas();
            show();
        }
    }

    public static void setXscale(double min, double max) {
        synchronized (LOCK) {
            xmin = min;
            xmax = max;
        }
    }

    public static void setYscale(double min, double max) {
        synchronized (LOCK) {
            ymin = min;
            ymax = max;
        }
    }

    public static void clear(Color color) {
        synchronized (LOCK) {
            init();
            offscreen.setBackground(color);
            offscreen.clearRect(0, 0, width, height);
            drawIfNeeded();
        }
    }

    public static void setPenColor(Color color) {
        synchronized (LOCK) {
            penColor = color;
            offscreen.setColor(color);
        }
    }

    public static void setFont(Font newFont) {
        synchronized (LOCK) {
            font = newFont;
            offscreen.setFont(newFont);
        }
    }

    public static void enableDoubleBuffering() {
        synchronized (LOCK) {
            defer = true;
        }
    }

    public static void show() {
        synchronized (LOCK) {
            if (panel != null) {
                SwingUtilities.invokeLater(() -> {
                    if (frame != null) {
                        frame.repaint();
                    }
                    panel.repaint();
                });
            }
        }
    }

    public static void pause(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void text(double x, double y, String text) {
        synchronized (LOCK) {
            offscreen.setColor(penColor);
            offscreen.setFont(font);
            FontMetrics metrics = offscreen.getFontMetrics();
            double xs = scaleXToPixel(x);
            double ys = scaleYToPixel(y);
            Rectangle2D bounds = metrics.getStringBounds(text, offscreen);
            float drawX = (float) (xs - bounds.getWidth() / 2.0);
            float drawY = (float) (ys + metrics.getAscent() / 2.0);
            offscreen.drawString(text, drawX, drawY);
            drawIfNeeded();
        }
    }

    public static void filledSquare(double x, double y, double halfLength) {
        synchronized (LOCK) {
            double xs = scaleXToPixel(x - halfLength);
            double ys = scaleYToPixel(y + halfLength);
            double side = factorX(2 * halfLength);
            double sideY = factorY(2 * halfLength);
            offscreen.fill(new Rectangle2D.Double(xs, ys, side, sideY));
            drawIfNeeded();
        }
    }

    public static void picture(double x, double y, String filename) {
        synchronized (LOCK) {
            Image image = new ImageIcon(filename).getImage();
            if (image == null || image.getWidth(null) < 0) {
                throw new IllegalArgumentException("Could not load image: " + filename);
            }
            int imageWidth = image.getWidth(null);
            int imageHeight = image.getHeight(null);
            int drawX = (int) Math.round(scaleXToPixel(x) - imageWidth / 2.0);
            int drawY = (int) Math.round(scaleYToPixel(y) - imageHeight / 2.0);
            offscreen.drawImage(image, drawX, drawY, null);
            drawIfNeeded();
        }
    }

    public static boolean hasNextKeyTyped() {
        synchronized (LOCK) {
            return !keysTyped.isEmpty();
        }
    }

    public static char nextKeyTyped() {
        synchronized (LOCK) {
            if (keysTyped.isEmpty()) {
                throw new IllegalStateException("No keys typed.");
            }
            return keysTyped.removeFirst();
        }
    }

    public static double mouseX() {
        synchronized (LOCK) {
            return mouseX;
        }
    }

    public static double mouseY() {
        synchronized (LOCK) {
            return mouseY;
        }
    }

    private static void drawIfNeeded() {
        if (!defer) {
            show();
        }
    }

    private static char mapSpecialKey(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            return '\n';
        }
        if (e.getKeyCode() == KeyEvent.VK_1 || e.getKeyCode() == KeyEvent.VK_NUMPAD1) {
            return START_SIGNAL;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            return 'w';
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            return 's';
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            return 'a';
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            return 'd';
        }
        return 0;
    }

    private static double scaleXToPixel(double x) {
        return width * (x - xmin) / (xmax - xmin);
    }

    private static double scaleYToPixel(double y) {
        return height * (ymax - y) / (ymax - ymin);
    }

    private static double scaleX(int pixelX) {
        return xmin + pixelX * (xmax - xmin) / width;
    }

    private static double scaleY(int pixelY) {
        return ymax - pixelY * (ymax - ymin) / height;
    }

    private static double factorX(double w) {
        return w * width / Math.abs(xmax - xmin);
    }

    private static double factorY(double h) {
        return h * height / Math.abs(ymax - ymin);
    }

    private static final class DrawPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            synchronized (LOCK) {
                g.drawImage(offscreenImage, 0, 0, null);
            }
        }
    }
}
