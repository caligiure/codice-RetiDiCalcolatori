package ese3_datagram;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.TimeZone;

public class TimeServerMod extends Thread {
    public static final int PORT = 3575;
    public static final int LENGTH = 256;
    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(PORT);
            int n = 1;
            while (n <= 10) {
                byte[] buf = new byte[LENGTH];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String request = new String(packet.getData());

                // produce response
                Calendar myCalendar = Calendar.getInstance(TimeZone.getTimeZone(request));
                String response = "Time in " + request + "> " + myCalendar.get(Calendar.HOUR) + ":" + myCalendar.get(Calendar.MINUTE);
                buf = response.getBytes();

                // send response
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                socket.send(packet);
                n++;

                // debug
                System.out.println("Time in " + request + "> " + myCalendar.get(Calendar.HOUR) + ":" + myCalendar.get(Calendar.MINUTE));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    } // run

    public static void main(String[]args) {
        TimeServerMod tsm = new TimeServerMod();
        tsm.start();
    }
}