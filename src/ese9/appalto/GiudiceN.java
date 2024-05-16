package ese9.appalto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

public class GiudiceN implements Giudice {
    public static final int N = 5;
    private Socket ente;

    public GiudiceN() {
        printMsg("Waiting for connection...");
        Richiesta req = getRichiesta();
        printMsg("Richiesta: " + req.toString());
        sendRequestMulticast(req);
        printMsg("Request sent to Participants");
        printMsg("Waiting for Participants offers...");
        List<Offerta> offerte = getOfferte();
        Offerta winner = selectWinner(offerte);
        printMsg("Winner: " + winner.toString());
        sendWinner(winner, ente);
        printMsg("Result sent to Ente and Participants");
    }

    public static void main(String[] args) { new GiudiceN(); }

    private void printMsg(String msg){ System.out.println(msg); }

    private Richiesta getRichiesta() {
        try {
            ServerSocket ss = new ServerSocket(ENTE_PORT);
            ente = ss.accept();
            printMsg("Connected to Ente: " + ente.getInetAddress()+":"+ ente.getPort());
            ObjectInputStream in = new ObjectInputStream(ente.getInputStream());
            Richiesta req = (Richiesta) in.readObject();
            ss.close();
            return req;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Offerta> getOfferte() {
        Socket socket = null;
        List<Offerta> offerte = new LinkedList<>();
        try {
            ServerSocket ss = new ServerSocket(PARTICIPANT_PORT);
            for(int i=0; i<N; i++) {
                socket = ss.accept();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Offerta of = (Offerta) in.readObject();
                offerte.add(of);
                in.close();
                socket.close();
            }
            return offerte;
        } catch (IOException | ClassNotFoundException e) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(e);
                }
            }
            e.printStackTrace();
            return offerte;
        }
    }

    private Offerta selectWinner (List<Offerta> offerte) {
        int winPrice = offerte.getFirst().price;
        Offerta winner = offerte.getFirst();
        for (Offerta o : offerte) {
            if (o.price < winPrice) {
                winPrice = o.price;
                winner = o;
            }
        }
        return winner;
    }

}
