package byow.lab12;

import java.io.Serializable;

public class Position implements Serializable {
    private int x;
    private int y;
    private Position pos;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setPosition(Position position) {
        this.pos = position;
    }

    public Position(Position ref, int xoffset, int yoffset) {
        this.x = ref.getX() + xoffset;
        this.y = ref.getY() + yoffset;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean inside(int x1, int x2, int y1, int y2) {
        return x1 <= x && x <= x2 && y1 <= y && y <= y2;
    }

    @Override
    public String toString() {
        return "(" +this.x + ", " +this.y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != this.getClass()) {
            return false;
        }
        Position other = (Position) o;
        return this.x == other.x && this.y == other.y;
    }
}
