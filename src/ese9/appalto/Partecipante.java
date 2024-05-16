package ese9.appalto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.StringTokenizer;

public class Partecipante extends Thread {
    private MulticastSocket ms;
    private final int price;
    private final int searchGaraID;

    public Partecipante(int price, int searchGaraID) {
        this.price = price;
        this.searchGaraID = searchGaraID;
    }

    private void printMsg(String msg){
        System.out.println(Thread.currentThread().threadId()+" - "+msg);
    }

    public void run() {
        Socket socket = null;
        try {
            printMsg("Waiting for connection...");
            ms = new MulticastSocket(Giudice.MULTICAST_PORT);
            ms.joinGroup(InetAddress.getByName(Giudice.MULTICAST_IP));
            Richiesta richiestaRicevuta = searchRichiesta();
            assert richiestaRicevuta != null;
            Offerta off = new Offerta((int)Thread.currentThread().threadId(), price, richiestaRicevuta.getGaraID());
            socket = new Socket(Giudice.SERVER_ADDR, Giudice.PARTICIPANT_PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(off);
            printMsg("Sent offer: " + off + " for Request: " + richiestaRicevuta);
            String response = (String) in.readObject();
            printMsg("Received response: " + response);
            String[] tokens = response.split(" ");
            if (tokens.length >= 1) if (tokens[0].equals("Error:")) return;
            socket.close();
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            ms.receive(packet);
            boolean done = false;
            while (!done) {
                String winner = new String(packet.getData()).trim();
                String initialToken = null;
                int partID = 0, price = 0, garaID = 0;
                StringTokenizer st = new StringTokenizer(winner, " -", false);
                if (st.hasMoreTokens()) initialToken = st.nextToken();
                if (initialToken.equals("Winner>>")) {
                    if (st.hasMoreTokens()) partID = Integer.parseInt(st.nextToken());
                    if (st.hasMoreTokens()) price = Integer.parseInt(st.nextToken());
                    if (st.hasMoreTokens()) garaID = Integer.parseInt(st.nextToken());
                    if (garaID == richiestaRicevuta.getGaraID()) {
                        printMsg("Winner: "+winner);
                        if (partID == Thread.currentThread().threadId()) printMsg("You have won the auction");
                        else printMsg("You have lost the auction");
                        ms.close();
                        done = true;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if(socket!=null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    public Richiesta searchRichiesta() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                ms.receive(packet);
                String req = new String(packet.getData()).trim();
                StringTokenizer st = new StringTokenizer(req, " -", false);
                if (st.hasMoreTokens()) {
                    String initialToken = st.nextToken();
                    if (initialToken.equals("Request>>")) {
                        printMsg(req);
                        String description = null;
                        int maxPrice = 0;
                        int garaID = 0;
                        if(st.hasMoreTokens()) description = st.nextToken();
                        if(st.hasMoreTokens()) maxPrice = Integer.parseInt(st.nextToken());
                        if(st.hasMoreTokens()) garaID = Integer.parseInt(st.nextToken());
                        if(searchGaraID == garaID)
                            return new Richiesta(description, maxPrice, 1, garaID);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i< GiudiceN.N; i++) {
            Partecipante p1 = new Partecipante((int)(Math.random()*1000), 1);
            p1.start();
            Partecipante p2 = new Partecipante((int)(Math.random()*50000), 2);
            p2.start();
        }
    }
}
