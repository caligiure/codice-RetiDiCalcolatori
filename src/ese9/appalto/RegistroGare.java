package ese9.appalto;

import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Consumer;

public class RegistroGare implements Iterable<Richiesta> {
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

    public boolean isTerminated(int garaID) { return registro.get(garaID).isTerminated(); }

    public Gara getGara(int garaID) { return registro.get(garaID); }

    public boolean searchGara(int garaID) { return registro.get(garaID)!=null; }

    public void makeOffer(Offerta off) { registro.get(off.getGaraID()).makeOffer(off); }

    @Override
    public Iterator<Richiesta> iterator() {
        return new Iterator<Richiesta>() {
            private final Iterator<Gara> it = registro.values().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Richiesta next() {
                return it.next().getRichiesta();
            }
        };
    }


    //public Socket getEnte(int garaID) { return registro.get(garaID).getEnte(); }

}
