package ese4.scommesseCavalli;

import java.net.InetAddress;

public class Scommessa {
    private final int ID_scommessa;
    private final int N_cavallo;
    private final long puntata;
    private final InetAddress scommettitore;
    private static int nextID=0;

    public Scommessa (int n_cavallo, long punt, InetAddress scommett){
        ID_scommessa=nextID++;
        N_cavallo=n_cavallo;
        puntata=punt;
        scommettitore=scommett;
    }//costruttore

    public boolean equals(Object o){
        if (!(o instanceof Scommessa))
            return false;
        Scommessa s=(Scommessa)o;
        return N_cavallo==s.N_cavallo;
    }//equals

    public int getCavallo(){
        return N_cavallo;
    }//getCavallo

    public int getID(){
        return ID_scommessa;
    }//getID

    public long getPuntata(){
        return puntata;
    }//getPuntata

    public InetAddress getScommettitore(){
        return scommettitore;
    }// getScommettitore

}//Scomessa class