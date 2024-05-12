package ese9.appalto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

public class GiudiceN implements Giudice {
    public static final int N = 5;
    private Socket remote_socket;

    public GiudiceN() {
        printMsg("Waiting for connection...");
        Richiesta req = getRichiesta();
        printMsg("Richiesta: " + req.toString());
        sendRichiesta(req);
        printMsg("Request sent to Participants");
        printMsg("Waiting for Participants offers...");
        List<Offerta> offerte = getOfferte();
        Offerta winner = selectWinner(offerte);
        printMsg("Winner: " + winner.toString());
        sendWinner(winner);
        printMsg("Result sent to Ente and Participants");
    }

    public static void main(String[] args) { new GiudiceN(); }

    private void printMsg(String msg){ System.out.println(msg); }

    private Richiesta getRichiesta() {
        try {
            ServerSocket ss = new ServerSocket(REM_PORT);
            remote_socket = ss.accept();
            printMsg("Connected to Ente: " + remote_socket.getInetAddress()+":"+remote_socket.getPort());
            ObjectInputStream in = new ObjectInputStream(remote_socket.getInputStream());
            Richiesta req = (Richiesta) in.readObject();
            ss.close();
            return req;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendRichiesta(Richiesta req) {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket();
            byte[] buf = req.toString().getBytes();
            DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(MULTICAST_IP), MULTICAST_PORT);
            socket.send(dp);
            socket.close();
        } catch (IOException e) {
            if (socket != null) {
                socket.close();
            }
            throw new RuntimeException(e);
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

    private void sendWinner(Offerta winner) {
        MulticastSocket socket = null;
        try {
            ObjectOutputStream out = new ObjectOutputStream(remote_socket.getOutputStream());
            out.writeObject(winner);
            out.close();
            remote_socket.close();
            socket = new MulticastSocket();
            byte[] buf = winner.toString().getBytes();
            DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(MULTICAST_IP), MULTICAST_PORT);
            socket.send(dp);
            socket.close();
        } catch (IOException e) {
            if (socket != null) {
                socket.close();
            }
            throw new RuntimeException(e);
        }
    }

}
