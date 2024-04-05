package ese4.scommesseCavalli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

public class BetServer{
    private final HashMap<Integer, Scommessa> scommesse;
    private final Calendar limite;
    private final int port;
    private final BetAccepter accepter;
    private BetDenyer denyer;

    public BetServer(int port, Calendar deadline){
        scommesse=new HashMap<Integer, Scommessa>();
        limite=deadline;
        this.port=port;
        accepter=new BetAccepter(port);
        accepter.start();
    }//costruttore

    class BetAccepter extends Thread{
        private int port;
        private ServerSocket serv;
        private boolean accept;

        public BetAccepter (int p){
            try{
                port=p;
                serv=new ServerSocket(port);
                accept=true;
            } catch (IOException ioe){
                ioe.printStackTrace();
            }
        }//costruttore

        public void run(){
            while(accept){
                try{
                    Calendar now=Calendar.getInstance();
                    serv.setSoTimeout((int)(limite.getTimeInMillis()-now.getTimeInMillis()));
                    Socket k=serv.accept();
                    BufferedReader in = new BufferedReader (new InputStreamReader (k.getInputStream()));
                    PrintWriter out = new PrintWriter (k.getOutputStream(), true );
                    // riceve la scommessa (nel formato "numcavallo puntata") e la inserisce nella lista scommesse
                    String line=in.readLine();
                    int pos=line.indexOf(" ");
                    int numCavallo=Integer.parseInt(line.substring(0,pos));
                    long puntata=Long.parseLong(line.substring(pos+1));
                    InetAddress ip=k.getInetAddress();
                    Scommessa s = new Scommessa(numCavallo,puntata,ip);
                    int key=s.getID();
                    scommesse.put(key,s);
                    //manda ok
                    out.println("Scommessa accettata.");
                    out.close();
                    k.close();
                    System.out.println("Ricevuta scommessa "+ip+" "+numCavallo+" "+puntata);
                } catch (SocketTimeoutException ste){
                    accept=false;
                    System.out.println("Tempo a disposizione per le scommesse terminato");
                }// catch
                catch (IOException ioe) {System.out.println(ioe);}
            }//while
            try{
                serv.close();
            } catch (IOException ioe) {System.out.println(ioe);}
        }// run

    }// BetAccepter class

    static class BetDenyer extends Thread{
        private int port;
        private ServerSocket serv;
        private boolean closed;

        public BetDenyer (int p){
            try{
                port=p;
                serv=new ServerSocket(port);
                closed=true;
            } catch (IOException ioe){
                System.out.println(ioe);}
        }//costruttore

        public void reset(){
            closed=false;
        }//reset

        public void run(){
            try{
                while(closed){
                    Socket k=serv.accept();
                    PrintWriter out = new PrintWriter(k.getOutputStream(),true);
                    out.println("Scommesse chiuse.");
                    out.close();
                    k.close();
                    System.out.println("Scommessa rifiutata.");
                }//while
                serv.close();
            } catch (IOException ioe) {System.out.println(ioe);}
        }// run

    }// BetDenyer

    public void accettaScommesse(){
        try{
            accepter.join();
        } catch (InterruptedException ie) { System.out.println(ie); }
    }//accettaScommesse

    public void rifiutaScommesse(){
        denyer= new BetDenyer(port);
        denyer.start();
    }//rifiutaScommesse

    public void resetServer(){
        denyer.reset();
    }//resetServer

    public LinkedList<Scommessa> controllaScommesse (int cavalloVincente){
        LinkedList<Scommessa> elenco=new LinkedList<Scommessa>();
        for (int i=0; i<scommesse.size(); i++){
            Scommessa s = scommesse.get(i);
            if (s.equals(new Scommessa(cavalloVincente,0,null)))
                elenco.addLast(s);
        }//for
        return elenco;
    }//controllaScommesse

    public void comunicaVincitori(LinkedList<Scommessa> vincitori, InetAddress ind, int port){
        ListIterator<Scommessa> it = vincitori.listIterator();
        try{
            MulticastSocket socket=new MulticastSocket();
            byte[] buf=new byte[256];
            StringBuilder m= new StringBuilder();
            int quota = 12; // moltiplicatore della puntata vincente
            while(it.hasNext()){
                Scommessa s=it.next();
                m.append(s.getScommettitore()).append(" ").append(s.getPuntata() * quota).append("\n");
            }
            buf= m.toString().getBytes();
            DatagramPacket pk=new DatagramPacket(buf, buf.length, ind, port);
            socket.send(pk);
        } catch (IOException ioe) {System.out.println(ioe);}
    }//comunicaVincitori

    public static void main (String[] args){
        int serverPort=8001; // su cui il BetServer riceve, tramite socket tcp, le scommesse
        int clientPort=8002; // su cui il BetServer invia ai client, in multicast, i risultati
        try{
            Calendar deadline=Calendar.getInstance();
            deadline.add(Calendar.SECOND,30); //la deadline è fissata tra un minuto
            BetServer server=new BetServer(serverPort, deadline);
            System.out.println("Scommesse aperte");
            server.accettaScommesse(); // accetta fino alla scadenza della deadline
            server.rifiutaScommesse(); // dopo la deadline ogni scommessa viene rifiutata
            int vincente=((int)(Math.random()*12))+1;
            System.out.println("E' risultato vincente il cavallo: "+vincente);
            LinkedList<Scommessa> elencoVincitori=server.controllaScommesse(vincente);
            InetAddress multiAddress=InetAddress.getByName("230.0.0.1");
            server.comunicaVincitori(elencoVincitori, multiAddress, clientPort);
            Thread.sleep(120000);
            server.resetServer();
        } catch (InterruptedException ie) { System.out.println(ie); }
        catch (UnknownHostException uhe) { System.out.println(uhe); }
    }// main

}// BetServer class