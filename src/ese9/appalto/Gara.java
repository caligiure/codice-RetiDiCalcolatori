package ese9.appalto;

import java.net.Socket;

public class Gara {
    private final int ID;
    private final Richiesta req;
    private final Socket ente;
    private boolean open;
    private Offerta winner;

    public Gara(int ID, Richiesta req, Socket ente) {
        this.ID = ID;
        this.req = req;
        this.ente = ente;
        open = false;
        winner = null;
    }

    public int getID() { return ID; }

    public Richiesta getRichiesta() { return req; }

    public Socket getEnte() { return ente; }

}
