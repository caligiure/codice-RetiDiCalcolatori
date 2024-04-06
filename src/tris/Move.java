package tris;

import java.io.Serializable;

public class Move implements Serializable {
    private final int x, y;

    public Move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}