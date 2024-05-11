package ese9.appaltoTimed;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class GiudiceTimed {
    public static final String SERVER_ADDR = "localhost";
    public static final int REM_PORT = 2000;
    public static final int MULTICAST_PORT = 3000;
    public static final String MULTICAST_IP = "230.0.0.1";
    public static final int PARTICIPANT_PORT = 4000;
    private Socket remote_socket;

    public GiudiceTimed() {
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

    public static void main(String[] args) { new GiudiceTimed(); }

    private void printMsg(String msg){ System.out.println(msg); }

    public static class Richiesta implements Serializable {
        public final String description;
        public final int price;

        Richiesta(String description, int price) {
            this.description = description;
            this.price = price;
        }

        public String toString() {
            return description + " - " + price;
        }
    }

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

    public static class Offerta implements Serializable {
        public final int id, price;

        Offerta(int id, int price) {
            this.id = id;
            this.price = price;
        }

        public String toString() {
            return id + " - " + price;
        }
    }

    private List<Offerta> getOfferte() {
        Socket socket = null;
        List<Offerta> offerte = new LinkedList<>();
        try {
            ServerSocket ss = new ServerSocket(PARTICIPANT_PORT);
            ss.setSoTimeout(60*1000);
            boolean moreTime = true;
            Calendar endTime = Calendar.getInstance();
            endTime.add(Calendar.MINUTE, 1);
            printMsg("Accepting offers (Time left: 1 minute)");
            while(moreTime) {
                socket = ss.accept();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Offerta of = (Offerta) in.readObject();
                offerte.add(of);
                in.close();
                socket.close();
                Calendar now = Calendar.getInstance();
                if(now.after(endTime)) {
                    moreTime = false;
                }
            }
            printMsg("Offers closed");
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
            printMsg("Offerta: " + o.toString());
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
