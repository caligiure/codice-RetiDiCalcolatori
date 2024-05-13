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

    public Socket getEnte(int garaID) { return registro.get(garaID).getEnte(); }

}
