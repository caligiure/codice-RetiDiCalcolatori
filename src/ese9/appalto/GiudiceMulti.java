package ese9.appalto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class GiudiceMulti implements Giudice {

    public static void main(String[] args) { new GiudiceMulti(); }

    private void printMsg(String msg){ System.out.println(msg); }

    private final RegistroGare registroGare;

    public GiudiceMulti() {
        registroGare = new RegistroGare();
        new RequestsAcceptor().start();
        new OffersAcceptor().start();
    }

    class RequestsAcceptor extends Thread {
        ServerSocket ss;
        @Override
        public void run() {
            try {
                ss = new ServerSocket(ENTE_PORT);
                while (true) {
                    Socket ente = ss.accept();
                    new RequestManager(ente).start();
                }
            } catch ( IOException e) {
                e.printStackTrace();
                try { if(ss!=null) ss.close(); } catch (IOException ex) { e.printStackTrace(); }
            }
        }
    }

    class RequestManager extends Thread {
        private final Socket ente;
        public RequestManager(Socket ente) { this.ente = ente; }
        @Override
        public void run() {
            try {
                ObjectOutputStream out = new ObjectOutputStream(ente.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(ente.getInputStream());
                Richiesta req = (Richiesta) in.readObject();
                printMsg("Received request: "+req+" from Ente: "+ente.getInetAddress()+":"+ente.getPort());
                int garaID = registroGare.addNewGara(req, ente);
                boolean started = registroGare.startGara(garaID);
                if (!started) {
                    String error = "Gara '"+registroGare.getGara(garaID)+"' can't be started";
                    out.writeObject(error);
                    printMsg(error);
                    ente.close();
                    return;
                }
                sendRequestMulticast(registroGare.getRichiesta(garaID));
                printMsg("Gara '"+registroGare.getGara(garaID)+"' started");
                int duration = registroGare.getDurationMIN(garaID);
                if (duration > 0) TimeUnit.MINUTES.sleep(duration);
                printMsg("Gara "+registroGare.getGara(garaID)+" ended");
                Offerta winner = registroGare.getWinner(garaID);
                out.writeObject(winner);
                printMsg("Winner: "+winner);
                ente.close();
                sendWinnerMulticast(winner);
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
                try { ente.close(); } catch (IOException ex) { ex.printStackTrace(); }
            }
        }
    }

    class OffersAcceptor extends Thread {
        ServerSocket ss;
        @Override
        public void run() {
            try {
                ss = new ServerSocket(PARTICIPANT_PORT);
                while (true) {
                    Socket partecipante = ss.accept();
                    new OfferManager(partecipante).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                try { if(ss!=null) ss.close(); } catch (IOException ex) { e.printStackTrace(); }
            }
        }
    }

    class OfferManager extends Thread {
        private final Socket partecipante;
        public OfferManager(Socket partecipante) { this.partecipante = partecipante; }
        @Override
        public void run() {
            try {
                ObjectOutputStream out = new ObjectOutputStream(partecipante.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(partecipante.getInputStream());
                Offerta off = (Offerta) in.readObject();
                printMsg("Received offer: "+off);
                boolean exists = registroGare.searchGara(off.getGaraID());
                if (!exists) out.writeObject("Error: Gara "+off.getGaraID()+" not found");
                else if (!registroGare.isOpen(off.getGaraID())) out.writeObject("Error: Gara "+off.getGaraID()+" is not open");
                else {
                    registroGare.makeOffer(off);
                    out.writeObject("Placed offer: "+off);
                }
                partecipante.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                try { partecipante.close(); } catch (IOException ex) { e.printStackTrace(); }
            }
        }
    }

}
