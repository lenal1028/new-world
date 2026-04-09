package byow.Core;

import byow.lab12.Position;

public class Room {
    Position upL;
    Position upR;
    Position downL;
    Position downR;
    int width;
    int height;
    int area;

    public Room(Position startPoint, int height, int width) {
        upL = startPoint;
        upR = new Position(startPoint.getX() + width, startPoint.getY());
        downL = new Position(startPoint.getX(), startPoint.getY() - height);
        downR = new Position(upR.getX(), downL.getY());
        this.width = width;
        this.height = height;
        area = width * height;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    //Check intersect
    //Leave 2 space for wall
    public boolean intersect(Room o) {
        int x1 = downL.getX() - 1;
        int x2 = upR.getX() + 1;
        int y1 = downL.getY() - 1;
        int y2 = upR.getY() + 1;
        if (o.downR.getY() > y2 || o.upL.getY() < y1) {
            return false;
        } else {
            return !(o.downL.getX() > x2 || o.downR.getX() < x1);
        }

//        return (o.upL.inside(x1 - 1, x2 + 1, y1 - 1, y2 + 1) ||
//                o.upR.inside(x1 - 1, x2 + 1, y1 - 1, y2 + 1) ||
//                o.downL.inside(x1 - 1, x2 + 1, y1 - 1, y2 + 1) ||
//                o.downR.inside(x1 - 1, x2 + 1, y1 - 1, y2 + 1));
    }

    public static void main(String[] args) {
        Room a = new Room(new Position(3, 6), 3, 2);
        Room b = new Room(new Position(7, 4), 2, 1);
        Room c = new Room(new Position(4, 7), 2, 2);
        Room d = new Room(new Position(1, 5), 1, 1);
        Room e = new Room(new Position(2, 5), 1, 1);
        Room f = new Room(new Position(4, 4), 1, 1);
        Boolean b1 = a.intersect(b);
        Boolean b2 = a.intersect(c);
        Boolean b3 = a.intersect(d);
        Boolean b4 = a.intersect(e);
        Boolean b5 = a.intersect(f);

    }
}
