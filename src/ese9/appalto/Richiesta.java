package ese9.appalto;

import java.io.Serializable;

public class Richiesta implements Serializable {
    public final String description;
    public final int maxPrice;
    private int durationMIN = 0;
    private int garaID = 0;

    Richiesta(String description, int price) {
        this.description = description;
        this.maxPrice = price;
    }

    Richiesta(String description, int maxPrice, int duration) {
        this.description = description;
        this.maxPrice = maxPrice;
        this.durationMIN = duration;
    }

    Richiesta(String description, int maxPrice, int duration, int garaID) {
        this.description = description;
        this.maxPrice = maxPrice;
        this.durationMIN = duration;
        this.garaID = garaID;
    }

    public String toString() {
        if(garaID==0) return description + " - " + maxPrice;
        else return description + " - " + maxPrice + " - "+garaID;
    }

    public int getDurationMIN() {return durationMIN;}

    public int getGaraID() {return garaID;}

}
