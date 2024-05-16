package ese9.appalto;

import java.net.Socket;
import java.util.HashMap;

public class RegistroGare {
    private final HashMap<Integer, Gara> registro;

    public RegistroGare() {
        registro = new HashMap<>();
    }

    public synchronized int addNewGara(Richiesta req, Socket ente) {
        int garaID = registro.size()+1;
        Gara g = new Gara(garaID, req, ente);
        registro.put(garaID, g);
        return garaID;
    }

    public Richiesta getRichiesta(int garaID) { return registro.get(garaID).getRichiesta(); }

    public synchronized boolean startGara(int garaID) { return registro.get(garaID).startGara(); }

    public boolean isOpen(int garaID) { return registro.get(garaID).isOpen(); }

    public Gara getGara(int garaID) { return registro.get(garaID); }

    public boolean searchGara(int garaID) { return registro.get(garaID)!=null; }

    public synchronized void makeOffer(Offerta off) { registro.get(off.getGaraID()).makeOffer(off); }

    public int getDurationMIN(int garaID) { return registro.get(garaID).getDurationMIN(); }

    public Offerta getWinner(int garaID) {
        return registro.get(garaID).getWinner();
    }
}
