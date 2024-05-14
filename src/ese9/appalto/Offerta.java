package ese9.appalto;

import java.io.Serializable;

public class Offerta implements Serializable {
    public final int partID, price;
    private int garaID = 0;

    Offerta(int partID, int price) {
        this.partID = partID;
        this.price = price;
    }

    Offerta(int partID, int price, int garaID) {
        this.partID = partID;
        this.price = price;
        this.garaID=garaID;
    }

    public String toString() {
        if(garaID==0) return "Partecipante " + partID + " - " + price;
        else return "Partecipante " + partID + " - " + price + " - gara "+garaID;
    }

    public int getGaraID() { return garaID; }

}
