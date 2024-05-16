package ese9.appalto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Ente extends Thread {
    Richiesta req;

    private void printMsg(String msg){
        System.out.println("Ente "+Thread.currentThread().threadId()+">> "+msg);
    }

    public Ente (Richiesta req) {
        this.req = req;
    }

    public void run () {
        try {
            printMsg("Waiting for connection...");
            Socket socket = new Socket(InetAddress.getByName(Giudice.SERVER_ADDR), Giudice.ENTE_PORT);
            printMsg("Connected to Giudice: "+socket.getInetAddress()+":"+socket.getPort());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(req);
            printMsg("Request sent to Giudice");
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Object obj = in.readObject();
            if (obj instanceof Offerta) {
                Offerta winner = (Offerta) obj;
                System.out.println("Winner: "+winner);
            } else if (obj instanceof String error) {
                printMsg("Error: "+error);
            }
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Ente(new Richiesta("Rotonda", 1000, 1)).start();
        new Ente(new Richiesta("Ponte", 50000, 1)).start();
    }

}
