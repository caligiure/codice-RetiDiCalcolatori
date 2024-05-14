package ese9.appalto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public interface Giudice {
    String SERVER_ADDR = "localhost";
    int ENTE_PORT = 2000;
    int MULTICAST_PORT = 3000;
    String MULTICAST_IP = "230.0.0.1";
    int PARTICIPANT_PORT = 4000;

    default void sendRequest(Richiesta req) {
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
            e.printStackTrace();
        }
    }

}
