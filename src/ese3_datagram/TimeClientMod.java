package ese3_datagram;

import java.io.IOException;
import java.net.*;

public class TimeClientMod extends Thread {
    public void run(){
        DatagramSocket socket = null;
        try{
            // produce request
            String request = "US/Alaska";
            byte[] buf = request.getBytes();

            // send request
            socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, TimeServerMod.PORT);
            socket.send(packet);

            // receive response
            buf = new byte[TimeServerMod.LENGTH];
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            // show response
            String received = new String(packet.getData());
            System.out.println("Response: " + received);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if(socket!=null)
                socket.close();
        }
    } // run

    public static void main(String[]args){
        TimeClientMod tcm = new TimeClientMod();
        tcm.start();
    }
}
