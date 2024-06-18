package tr2024_03_14;

public class StatoSensore {
    private final int ID_sensore;
    private int NUM_stato;
    private final int temp;
    private final int umid;

    public StatoSensore(int sensore, int id, int temp, int umid) {
        this.ID_sensore = sensore;
        this.NUM_stato = id;
        this.temp = temp;
        this.umid = umid;
    }

    public int getID_sensore() {
        return ID_sensore;
    }

    public int getUmid() {
        return umid;
    }

    public int getTemp() {
        return temp;
    }

    public int getNUM_stato() {
        return NUM_stato;
    }

    public void setNUM_stato(int NUM_stato) { this.NUM_stato = NUM_stato; }

}
