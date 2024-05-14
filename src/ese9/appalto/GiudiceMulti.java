package ese9.appalto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;

public class GiudiceMulti implements Giudice {

    public static void main(String[] args) { new GiudiceTimed(); }

    private void printMsg(String msg){ System.out.println(msg); }

    private RegistroGare registroGare;

    public GiudiceMulti() {
        new EnteAcceptor().start();
    }

    class EnteAcceptor extends Thread {
        @Override
        public void run() {
            try {
                ServerSocket ss = new ServerSocket(ENTE_PORT);
                while (true) {
                    Socket ente = ss.accept();
                    new RequestManager(ente).start();
                    new OffersGetter().start();
                    new WinSender().start();
                }
            } catch ( IOException e) { e.printStackTrace(); }
        }
    }

    class RequestManager extends Thread {
        private final Socket ente;
        public RequestManager(Socket ente) { this.ente = ente; }
        @Override
        public void run() {
            try {
                printMsg("Connected to Ente: " + ente.getInetAddress()+":"+ente.getPort());
                ObjectInputStream in = new ObjectInputStream(ente.getInputStream());
                Richiesta req = (Richiesta) in.readObject();
                printMsg("Received request: "+req+" from Ente: "+ente.getInetAddress()+":"+ente.getPort());
                int garaID = registroGare.addNewGara(req, ente);
                sendRequest(registroGare.getRichiesta(garaID));
                boolean started = registroGare.startGara(garaID);
                if (started) printMsg("Gara "+registroGare.getGara(garaID)+" started");
                else printMsg("Gara "+registroGare.getGara(garaID)+" can't be started");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    class OffersGetter extends Thread {
        @Override
        public void run() {
            try {
                ServerSocket ss = new ServerSocket(PARTICIPANT_PORT);
                while (true) {
                    Socket socket = ss.accept();
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    Offerta off = (Offerta) in.readObject();
                    printMsg("Received offer: "+off);
                    boolean exists = registroGare.searchGara(off.getGaraID());
                    if (!exists) printMsg("Gara "+off.getGaraID()+" not found");
                    else if (!registroGare.isOpen(off.getGaraID())) printMsg("Gara "+off.getGaraID()+" is not open");
                    else registroGare.makeOffer(off);
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class WinSender extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    for(Richiesta req : registroGare) {

                    }
                }
            }
        }
    }

}
