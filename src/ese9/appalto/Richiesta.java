package ese9.appalto;

import java.io.Serializable;

public class Richiesta implements Serializable {
    public final String description;
    public final int price;

    Richiesta(String description, int price) {
        this.description = description;
        this.price = price;
    }

    public String toString() {
        return description + " - " + price;
    }

}
