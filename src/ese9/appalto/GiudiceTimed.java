package ese9.appalto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class GiudiceTimed implements Giudice {
    private Socket remote_socket;

    public GiudiceTimed() {
        printMsg("Waiting for connection...");
        Richiesta req = getRichiesta();
        printMsg("Richiesta: " + req.toString());
        sendRequest(req);
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

    private Richiesta getRichiesta() {
        try {
            ServerSocket ss = new ServerSocket(ENTE_PORT);
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

    private List<Offerta> getOfferte() {
        List<Offerta> offerte = new LinkedList<>();
        try {
            ServerSocket ss = new ServerSocket(PARTICIPANT_PORT);
            ss.setSoTimeout(60*1000);
            boolean moreTime = true;
            Calendar endTime = Calendar.getInstance();
            endTime.add(Calendar.MINUTE, 1);
            printMsg("Accepting offers (Time left: 1 minute)");
            while(moreTime) {
                Socket socket = ss.accept();
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
