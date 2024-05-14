package ese9.appalto;

import java.net.Socket;
import java.util.Calendar;

public class Gara {
    private final Richiesta richiesta;
    private final Socket ente;
    private boolean started;
    private Offerta winner;
    private Calendar endTime;

    public Gara(int garaID, Richiesta req, Socket ente) {
        richiesta = new Richiesta(req.description, req.maxPrice, req.getDuration(), garaID);
        this.ente = ente;
        started = false;
    }

    public Richiesta getRichiesta() { return richiesta; }

    public boolean startGara() {
        if (started) return false;
        started = true;
        Calendar startTime = Calendar.getInstance();
        startTime.add(Calendar.MINUTE, richiesta.getDuration());
        endTime = startTime;
        return true;
    }

    public boolean isOpen() { return started && Calendar.getInstance().before(endTime); }

    public boolean isTerminated() { return started && Calendar.getInstance().after(endTime); }

    public String toString() { return richiesta.toString(); }

    public void makeOffer(Offerta off) {
        if(!isOpen()) return;
        if(winner == null) winner=off;
        else if(off.price < winner.price) winner=off;
    }

}
