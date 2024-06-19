package traccia_2023_07_11;

import java.io.Serializable;

public class Offerta implements Serializable, Comparable<Offerta> {
    private final int IVA_NEGOZ;
    private final String NAZI;
    private final int COD_PROD;
    private final double PREZ;
    private final int QUANT;

    public Offerta(int IVA, String NAZI, int COD_PROD, double PREZ, int QUANT) {
        this.IVA_NEGOZ = IVA;
        this.NAZI = NAZI;
        this.COD_PROD = COD_PROD;
        this.PREZ = PREZ;
        this.QUANT = QUANT;
    }

    public int getIVA_NEGOZ() {
        return IVA_NEGOZ;
    }

    public String getNAZI() {
        return NAZI;
    }

    public int getCOD_PROD() {
        return COD_PROD;
    }

    public double getPREZ() {
        return PREZ;
    }

    public int getQUANT() {
        return QUANT;
    }

    @Override
    public String toString() {
        return "Offerta{" +
                "IVA=" + IVA_NEGOZ +
                ", NAZI='" + NAZI + '\'' +
                ", COD_PROD=" + COD_PROD +
                ", PREZ=" + PREZ +
                ", QUANT=" + QUANT +
                '}';
    }

    @Override
    public int compareTo(Offerta o) {
        return Double.compare(PREZ, o.getPREZ());
    }

    public String toFormattedString() {
        return getCOD_PROD()+"#"+getPREZ()+"#"+getIVA_NEGOZ()+"#"+getNAZI()+"#"+getQUANT();
    }

}
