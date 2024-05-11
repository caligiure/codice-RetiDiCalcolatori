package ese9.appalto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Ente {

    private void printMsg(String msg){
        System.out.println(msg);
    }

    public Ente (Richiesta req) {
        try {
            printMsg("Waiting for connection...");
            Socket socket = new Socket(InetAddress.getByName(Giudice.SERVER_ADDR), Giudice.REM_PORT);
            printMsg("Connected to Giudice: "+socket.getInetAddress()+":"+socket.getPort());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(req);
            printMsg("Request sent to Giudice");
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Offerta winner = (Offerta) in.readObject();
            System.out.println("Winner: "+winner);
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Ente(new Richiesta("Rotonda", 1000));
    }
}
