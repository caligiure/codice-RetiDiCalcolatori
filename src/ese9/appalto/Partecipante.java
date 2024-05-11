package ese9.appalto;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

public class Partecipante extends Thread {
    private final int price;

    public Partecipante(int price) {
        this.price = price;
    }

    private void printMsg(String msg){
        System.out.println(Thread.currentThread().threadId()+" - "+msg);
    }

    public void run() {
        try {
            printMsg("Waiting for connection...");
            MulticastSocket ms = new MulticastSocket(Giudice.MULTICAST_PORT);
            ms.joinGroup(InetAddress.getByName(Giudice.MULTICAST_IP));
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            ms.receive(packet);
            String req = new String(packet.getData()).trim();
            printMsg("Richiesta dell'ente: "+req);
            Giudice.Offerta off = new Giudice.Offerta((int)Thread.currentThread().threadId(), price);
            Socket socket = new Socket(Giudice.SERVER_ADDR, Giudice.PARTICIPANT_PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(off);
            printMsg("Sent offer: "+off.price);
            socket.close();
            buffer = new byte[1024];
            packet = new DatagramPacket(buffer, buffer.length);
            ms.receive(packet);
            String winner = new String(packet.getData()).trim();
            printMsg("Winner: "+winner);
            ms.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        for (int i=0; i<Giudice.n; i++) {
            Partecipante p = new Partecipante( (int)(Math.random()*1000) );
            p.start();
        }
    }
}
