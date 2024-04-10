package tris;

import java.io.Serializable;

public class Move implements Serializable {
    private final int x, y;

    public Move(int k) {
        switch (k) {
            case 1: x=0; y=0; break;
            case 2: x=0; y=1; break;
            case 3: x=0; y=2; break;
            case 4: x=1; y=0; break;
            case 5: x=1; y=1; break;
            case 6: x=1; y=2; break;
            case 7: x=2; y=0; break;
            case 8: x=2; y=1; break;
            case 9: x=2; y=2; break;
            default: x=-1; y=-1; break;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}