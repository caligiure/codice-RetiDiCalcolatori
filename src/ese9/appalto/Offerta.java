package ese9.appalto;

import java.io.Serializable;

public class Offerta implements Serializable {
    public final int id, price;

    Offerta(int id, int price) {
        this.id = id;
        this.price = price;
    }

    public String toString() {
        return id + " - " + price;
    }
}
