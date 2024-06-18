package tr2024_03_14;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class Sensore {

    public static void main(String[] args) {
        new Sensore(1);
    }

    private void printinfo(String s) { System.out.println(s); }

    public Sensore(int ID_sensore) {
        StatoSensore s = new StatoSensore(ID_sensore, 25, 10);
        printinfo("Stato sensore: " + ID_sensore);
        sendStatus(s);
        sendRequest(ID_sensore);
        getNotification();
    }

    private void sendStatus(StatoSensore s) {
        Socket sock = null;
        try {
            sock = new Socket(InetAddress.getByName(Server.HOSTNAME), Server.TCP_PORT_StatoSensore);
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
        Socket s = null;
        try {
            s = new Socket(InetAddress.getByName(Server.HOSTNAME), Server.TCP_PORT_RequestNotification);
            PrintWriter out = new PrintWriter( s.getOutputStream() );
            BufferedReader in = new BufferedReader( new InputStreamReader( s.getInputStream() ) );
            out.println("Notifica aggiornamenti al sensore: ID="+id);
            String resp = in.readLine();
            printinfo(resp);
            s.close();
        } catch ( IOException e ) {
            if ( s!=null ){
                try {
                    s.close();
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
                String msg = new String( dp.getData() );
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
