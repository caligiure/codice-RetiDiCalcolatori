package ese4.scommesseCavalli_obj;

import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

public class BetServerObject {
    private HashMap<Integer, Scommessa> scommesse;
    private Calendar limite;
    private BetAccepter accepter;
    private BetDenyer denyer;
    private int port;

    public BetServerObject(int port, Calendar deadline) {
        scommesse = new HashMap<Integer, Scommessa>();
        limite = deadline;
        this.port = port;
        accepter = new BetAccepter(port);
        accepter.start();
    }//costruttore


    class BetAccepter extends Thread {
        private int port;
        private ServerSocket serv;
        private boolean accept;

        public BetAccepter(int p) {
            try {
                port = p;
                serv = new ServerSocket(port);
                accept = true;
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }//costruttore

        public void run() {
            while (accept) {
                try {
                    Calendar now = Calendar.getInstance();
                    serv.setSoTimeout((int) (limite.getTimeInMillis() - now.getTimeInMillis()));
                    Socket k = serv.accept();

                    ObjectOutputStream out = new ObjectOutputStream(k.getOutputStream());
                    ObjectInputStream in =
                            new ObjectInputStream(k.getInputStream());

                    // riceve la scommessa (nel formato "numcavallo puntata") e la inserisce nella lista scommesse
                    Scommessa scommessa = (Scommessa) in.readObject();

                    int numCavallo = scommessa.getCavallo();
                    long puntata = scommessa.getPuntata();

                    InetAddress ip = k.getInetAddress();
                    scommesse.put(scommessa.getID(), scommessa);

                    //manda ok
                    out.writeObject("Scommessa accettata.");
                    out.close();
                    k.close();
                    System.out.println("Ricevuta scommessa " + ip + " " + numCavallo + " " + puntata);

                } catch (SocketTimeoutException ste) {
                    accept = false;
                    System.out.println("Tempo a disposizione per le scommesse terminato");
                }// catch
                catch (IOException ioe) {
                    System.out.println(ioe);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }//while
            try {
                serv.close();
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }// run

    }// BetAccepter class

    class BetDenyer extends Thread {
        private int port;
        private ServerSocket serv;
        private boolean closed;

        public BetDenyer(int p) {
            try {
                port = p;
                serv = new ServerSocket(port);
                closed = true;
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }//costruttore

        public void reset() {
            closed = false;
        }//reset

        public void run() {
            try {
                while (closed) {
                    Socket k = serv.accept();
                    ObjectOutputStream out = new ObjectOutputStream(k.getOutputStream());

                    out.writeObject("Scommesse chiuse.");
                    out.close();
                    k.close();
                    System.out.println("Scommessa rifiutata.");
                }//while
                serv.close();
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        }// run

    }// BetDenyer

    public void accettaScommesse() {
        try {
            accepter.join();
        } catch (InterruptedException ie) {
            System.out.println(ie);
        }
    }//accettaScommesse

    public void rifiutaScommesse() {
        denyer = new BetDenyer(port);
        denyer.start();
    }//rifiutaScommesse

    public void resetServer() {
        denyer.reset();
    }//resetServer

    public LinkedList<Scommessa> controllaScommesse(int cavalloVincente) {
        LinkedList<Scommessa> elenco = new LinkedList<Scommessa>();
        for (int i = 0; i < scommesse.size(); i++) {
            Scommessa s = scommesse.get(i);
            if (s.equals(new Scommessa(cavalloVincente, 0, null)))
                elenco.addLast(s);
        }//for
        return elenco;
    }//controllaScommesse

    public void comunicaVincitori(LinkedList<Scommessa> vincitori, InetAddress ind, int port) {
        ListIterator<Scommessa> it = vincitori.listIterator();
        try {
            MulticastSocket socket = new MulticastSocket();
            byte[] buf = new byte[256];
            String m = "";
            int quota = 12; // moltiplicatore della puntata vincente
            while (it.hasNext()) {
                Scommessa s = it.next();
                m += s.getScommettitore() + " " + (s.getPuntata() * quota) + "\n";
            }
            buf = m.getBytes();
            DatagramPacket pk = new DatagramPacket(buf, buf.length, ind, port);
            socket.send(pk);
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }//comunicaVincitori

    public static void main(String[] args) {
        int serverPort = 8001; // su cui il BetServer riceve, tramite socket tcp, le scommesse
        int clientPort = 8002; // su cui il BetServer invia ai client, in multicast, i risultati
        try {
            Calendar deadline = Calendar.getInstance();
            deadline.add(Calendar.MINUTE, 1); //la deadline è fissata tra un minuto
            BetServerObject server = new BetServerObject(serverPort, deadline);
            System.out.println("Scommesse aperte");
            server.accettaScommesse(); // accetta fino alla scadenza della deadline
            server.rifiutaScommesse(); // dopo la deadline ogni scommessa viene rifiutata
            int vincente = ((int) (Math.random() * 12)) + 1;
            System.out.println("E' risultato vincente il cavallo: " + vincente);
            LinkedList<Scommessa> elencoVincitori = server.controllaScommesse(vincente);
            InetAddress multiAddress = InetAddress.getByName("230.0.0.1");
            server.comunicaVincitori(elencoVincitori, multiAddress, clientPort);
            Thread.sleep(120000);
            server.resetServer();
        } catch (InterruptedException ie) {
            System.out.println(ie);
        } catch (UnknownHostException uhe) {
            System.out.println(uhe);
        }
    }// main

}// BetServer class

