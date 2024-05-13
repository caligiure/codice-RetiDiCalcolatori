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
                    new RequestGetter(ente).start();
                }
            } catch ( IOException e) { e.printStackTrace(); }
        }
    }

    class RequestGetter extends Thread {
        private final Socket ente;
        public RequestGetter(Socket ente) { this.ente = ente; }
        @Override
        public void run() {
            try {
                printMsg("Connected to Ente: " + ente.getInetAddress()+":"+ente.getPort());
                ObjectInputStream in = new ObjectInputStream(ente.getInputStream());
                Richiesta req = (Richiesta) in.readObject();
                printMsg("Received request: "+req+" from Ente: "+ente.getInetAddress()+":"+ente.getPort());
                int garaID = registroGare.addNewGara(req, ente);
                MulticastSocket ms = new MulticastSocket();
                byte[] buf = registroGare.getRichiesta(garaID).toString().getBytes();
                DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(MULTICAST_IP), MULTICAST_PORT);
                ms.send(dp);
                ms.close();

                new RequestSender(garaID).start();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    class RequestSender extends Thread {
        private final int garaID;
        public RequestSender(int garaID) { this.garaID = garaID; }
        @Override
        public void run() {
            MulticastSocket ms = null;
            try {
                ms = new MulticastSocket();
                byte[] buf = registroGare.getRichiesta(garaID).toString().getBytes();
                DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(MULTICAST_IP), MULTICAST_PORT);
                ms.send(dp);
                ms.close();
            } catch (IOException e) { if (ms != null) ms.close(); e.printStackTrace(); }
        }
    }

}
