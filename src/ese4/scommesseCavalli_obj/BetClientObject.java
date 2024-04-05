package ese4.scommesseCavalli_obj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class BetClientObject {
    private int serverPort;
    private int myPort;
    private InetAddress groupAddress;
    private InetAddress serverAddress;
    private Socket s;

    public BetClientObject(InetAddress gAddress, InetAddress server, int sPort, int mPort) {
        groupAddress = gAddress;
        serverAddress = server;
        serverPort = sPort;
        myPort = mPort;
        try {
            s = new Socket(serverAddress, serverPort);
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    } // costruttore

    public boolean placeBet(int nCavallo, long puntata) {
        String e = "";
        try {

            Scommessa scommessa = new Scommessa(nCavallo, puntata, s.getLocalAddress());

            // Utilizzo Object stream per scrivere e leggere oggetti
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream in =
                    new ObjectInputStream(s.getInputStream());

            out.writeObject(scommessa); // invia l'oggetto scommessa

            e = (String) in.readObject(); // riceve un msg di risposta, che indica se la scommessa è stata accettata
            System.out.println(e);
            // oppure rifiutata (per esempio, se è arrivata oltre il tempo limite)
        } catch (IOException | ClassNotFoundException ioe) {
            ioe.printStackTrace();
        }
        return e.equals("Scommessa accettata.");
    }// placeBet

    public void riceviElencoVincitori() {
        try {
            MulticastSocket socket = new MulticastSocket(myPort);
            socket.joinGroup(groupAddress);
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String elenco = new String(packet.getData());
            System.out.print("Elenco vincitori: ");
            System.out.println(elenco);
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }// riceviElenco

    public static void main(String[] args) {
        int serverPort = 8001;
        int myPort = 8002;
        try {
            InetAddress group = InetAddress.getByName("230.0.0.1");
            InetAddress server = InetAddress.getByName("127.0.0.1");
            BetClientObject client = new BetClientObject(group, server, serverPort, myPort);
            int cavallo = ((int) (Math.random() * 12)) + 1; // cavallo su cui scommette, tra 1 e 12
            int cifra = ((int) (Math.random() * 100)) + 1; // cifra su cui scommette, tra 1 e 100 euro
            System.out.format("Invio scomessa sul cavallo %s di %s euro%n", cavallo, cifra);
            if (client.placeBet(cavallo, cifra))
                client.riceviElencoVincitori();
        } catch (UnknownHostException uhe) {
            System.out.println(uhe);
        }
    }// main

}// BetClient