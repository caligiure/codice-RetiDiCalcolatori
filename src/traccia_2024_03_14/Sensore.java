package traccia_2024_03_14;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class Sensore extends Thread {
    StatoSensore s;

    public static void main(String[] args) {
        new Sensore(1, 25, 10).start();
        new Sensore(2, 15, 3).start();
        new Sensore(3, 15, 4).start();
        new Sensore(4, 3, 0).start();
        new Sensore(5, 30, 20).start();
    }

    private void printinfo(String s) { System.out.println(s); }

    public Sensore(int ID_sensore, int temp, int umid) {
        s = new StatoSensore(ID_sensore, temp, umid);
    }

    @Override
    public void run() {
        printinfo("Stato sensore: " + s.toString() );
        sendStatus(s);
        sendRequest(s.getID_sensore());
        getNotification();
    }

    private void sendStatus(StatoSensore s) {
        Socket sock = null;
        try {
            sock = new Socket(InetAddress.getByName("localhost"), Server.TCP_PORT_StatoSensore);
            ObjectOutputStream out = new ObjectOutputStream( sock.getOutputStream() );
            BufferedReader in = new BufferedReader( new InputStreamReader( sock.getInputStream() ) );
            out.writeObject( s );
            String resp = in.readLine();
            StringTokenizer st = new StringTokenizer(resp, ":", false);
            printinfo(resp);
            sock.close();
        } catch ( IOException e ) {
            if (sock!=null) {
                try {
                    sock.close();
                } catch (IOException ex) {
                    e.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    private void sendRequest(int id) {
        Socket sock = null;
        try {
            sock = new Socket(InetAddress.getByName("localhost"), Server.TCP_PORT_RequestNotification);
            PrintWriter out = new PrintWriter( sock.getOutputStream(), true);
            BufferedReader in = new BufferedReader( new InputStreamReader( sock.getInputStream() ) );
            out.println("Notifica aggiornamenti al sensore: ID="+id);
            printinfo("Sensore "+id+" ha chiesto aggiornamenti");
            String resp = in.readLine();
            printinfo(resp);
            sock.close();
        } catch ( IOException e ) {
            if ( sock!=null ){
                try {
                    sock.close();
                } catch ( IOException e1 ) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    private void getNotification() {
        MulticastSocket ms = null;
        try {
            ms = new MulticastSocket(Server.UDP_PORT_Notification);
            while (true) {
                byte[] buf = new byte[256];
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                ms.receive(dp);
                String msg = new String( dp.getData() ).trim();
                String[] arr = msg.split("#");
                if (arr.length == 4)
                    printinfo("Received notification status: ID_sensore=" + arr[0] +
                        " NUM_stato=" + arr[1] + " Temp=" + arr[2] + " Umid=" + arr[3] );
                else
                    printinfo("Received notification: " + msg);
            }
        } catch ( IOException e ){
            if(ms!=null)
                ms.close();
            e.printStackTrace();
        }
    }

}
