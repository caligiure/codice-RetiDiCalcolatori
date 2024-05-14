package ese9.appalto;

import java.io.Serializable;

public class Richiesta implements Serializable {
    public final String description;
    public final int maxPrice;
    private int duration = 0;
    private int garaID = 0;

    Richiesta(String description, int price) {
        this.description = description;
        this.maxPrice = price;
    }

    Richiesta(String description, int price, int duration) {
        this.description = description;
        this.maxPrice = price;
        this.duration = duration;
    }

    Richiesta(String description, int price, int duration, int garaID) {
        this.description = description;
        this.maxPrice = price;
        this.duration = duration;
        this.garaID = garaID;
    }

    public String toString() {
        if(garaID==0) return description + " - " + maxPrice;
        else return description + " - " + maxPrice + " - gara "+garaID;
    }

    public int getGaraID() {return garaID;}

    public int getDuration() {return duration;}

}
