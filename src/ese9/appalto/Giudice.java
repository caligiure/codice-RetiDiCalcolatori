package ese9.appalto;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

public interface Giudice {
    String SERVER_ADDR = "localhost";
    int ENTE_PORT = 2000;
    int MULTICAST_PORT = 3000;
    String MULTICAST_IP = "230.0.0.1";
    int PARTICIPANT_PORT = 4000;

    default void sendRequestMulticast(Richiesta req) {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket();
            String str = "Request>> " + req.toString();
            byte[] buf = str.getBytes();
            DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(MULTICAST_IP), MULTICAST_PORT);
            socket.send(dp);
            socket.close();
        } catch (IOException e) { if (socket != null) { socket.close(); } e.printStackTrace(); }
    }

    default void sendWinnerMulticast(Offerta win) {
        MulticastSocket ms = null;
        try {
            ms = new MulticastSocket();
            String str = "Winner>> " + win.toString();
            byte[] buf = str.getBytes();
            DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(MULTICAST_IP), MULTICAST_PORT);
            ms.send(dp);
            ms.close();
        } catch (IOException e) { if (ms != null) { ms.close(); } e.printStackTrace(); }
    }

    default void sendWinner(Offerta win, Socket ente) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(ente.getOutputStream());
            out.writeObject(win);
            ente.close();
            sendWinnerMulticast(win);
        } catch (IOException e) { e.printStackTrace(); }
    }

}
