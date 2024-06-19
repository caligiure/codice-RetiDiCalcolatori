package traccia_2023_07_11;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    public static final int TCP_OFF_PORT = 3000;
    public static final int NEG_NOTIFICATION_PORT = 5000;
    public static final String NEG_NOTIFICATION_IP = "230.0.0.1";
    public static final int TCP_CLI_PORT = 4000;
    public static final int CLI_NOTIFICATION_PORT = 6000;
    private final HashMap<String, TreeSet<Offerta>> mappaOfferte = new HashMap<>();
    private final Map<String, List<String>> clientiRegistrati = new HashMap<>();

    public Server() {
        new OfferAcceptor().start();
        new ClientAcceptor().start();
    }

    private void printinfo(String s) {
        System.out.println(s);
    }

    private class OfferAcceptor extends Thread {
        @Override
        public void run() {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(TCP_OFF_PORT);
                while (true) {
                    Socket negozio = ss.accept();
                    new OfferHandler(negozio).start();
                }
            } catch (IOException e) {
                if (ss != null)
                    try {
                        ss.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                e.printStackTrace();
            }
        }
    }

    private class OfferHandler extends Thread {
        Socket negozio;
        public OfferHandler(Socket negozio) {
            this.negozio = negozio;
        }
        public void run() {
            try {
                ObjectInputStream in = new ObjectInputStream(negozio.getInputStream());
                Offerta off = (Offerta) in.readObject();
                boolean bestOffer = false;
                if (off.getQUANT() > 0) {
                    bestOffer = addOfferta(off);
                    printinfo(off + " registrata");
                } else {
                    removeOfferta(off);
                    printinfo(off + " rimossa" );
                }
                if (bestOffer) {
                    sendBestOffer(off);
                }
                negozio.close();
            } catch (IOException | ClassNotFoundException e) {
                try {
                    negozio.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }

    synchronized private boolean addOfferta(Offerta off) {
        TreeSet<Offerta> setOfferte = mappaOfferte.get( off.getNAZI() );
        if (setOfferte == null) {
            setOfferte = new TreeSet<>();
            setOfferte.add(off);
            mappaOfferte.put( off.getNAZI(), setOfferte );
            return true;
        }
        Iterator<Offerta> it = setOfferte.iterator();
        Offerta best = null;
        while ( it.hasNext() ) {
            Offerta off1 = it.next();
            if ( off1.getCOD_PROD() == off.getCOD_PROD() ) {
                best = off1;
                break;
            }
        }
        setOfferte.add(off);
        if (best == null)
            return true;
        else
            return off.compareTo(best) <0;
    }

    synchronized private void removeOfferta(Offerta off) {
        TreeSet<Offerta> setOfferte = mappaOfferte.get( off.getNAZI() );
        if (setOfferte == null)
            return;
        Iterator<Offerta> it = setOfferte.iterator();
        while ( it.hasNext() ) {
            Offerta off1 = it.next();
            if ( off1.getCOD_PROD()==off.getCOD_PROD() &&
                    off1.getIVA_NEGOZ() == off.getIVA_NEGOZ() && off1.getPREZ()==off.getPREZ())
                it.remove();
        }
    }

    synchronized private void sendBestOffer( Offerta off ) {
        MulticastSocket ms =null;
        DatagramSocket ds = null;
        try {
            ms = new MulticastSocket();
            String msg = off.toFormattedString();
            byte[] buf = msg.getBytes();
            DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName(NEG_NOTIFICATION_IP), NEG_NOTIFICATION_PORT);
            ms.send(dp); // notifica negozi
            ms.close();
            ds = new DatagramSocket();
            List<String> listaClienti = clientiRegistrati.get(off.getNAZI());
            for( String c : listaClienti) {
                String[] parts = c.split("#"); // ip+"#"+cod_prod
                int cod_prod = Integer.parseInt(parts[1]);
                if( cod_prod == off.getCOD_PROD() ) {
                    InetAddress ip = InetAddress.getByName(parts[0]);
                    dp = new DatagramPacket(buf, buf.length, ip, CLI_NOTIFICATION_PORT);
                    ds.send(dp);
                }
            }
            ds.close();
        } catch ( IOException e ) {
            if (ms != null) ms.close();
            if (ds != null) ds.close();
            e.printStackTrace();
        }
    }

    synchronized private Offerta searchOfferta(int cod_prod, String nazi) {
        TreeSet<Offerta> setOfferte = mappaOfferte.get( nazi );
        if (setOfferte == null)
            return null;
        for( Offerta off : setOfferte) {
            if(off.getCOD_PROD() == cod_prod)
                return off;
        }
        return null;
    }

    synchronized private void registraCliente(InetAddress ip, int cod_prod, String nazi) {
        String cliente = ip+"#"+cod_prod;
        List<String> listaClienti = clientiRegistrati.get( nazi );
        if(listaClienti == null) {
            listaClienti = new LinkedList<>();
            clientiRegistrati.put(nazi, listaClienti);
        }
        listaClienti.add(cliente);
    }

    private class ClientAcceptor extends Thread {
        @Override
        public void run() {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(TCP_CLI_PORT);
                while (true) {
                    Socket client = ss.accept();
                    new ClientHandler(client).start();
                }
            } catch (IOException e) {
                if (ss != null)
                    try {
                        ss.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler extends Thread {
        Socket client;
        public ClientHandler(Socket client) {
            this.client = client;
        }
        public void run() {
            try {
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String msg = in.readLine();
                String[] parts = msg.split("#");
                if( parts.length != 3 ) {
                    out.println("");
                    client.close();
                    return;
                }
                int cod_prod = Integer.parseInt(parts[0]);
                String nazi = parts[1];
                boolean registrami = parts[2].equals("true");
                Offerta off = searchOfferta(cod_prod, nazi);
                if( off==null )
                    out.println("");
                else
                    out.println(off.toFormattedString());
                if(registrami) {
                    registraCliente(client.getInetAddress(), cod_prod, nazi);
                }
                client.close();
            } catch ( IOException e ) {
                try {
                    if( client!=null )
                        client.close();
                } catch (IOException ex) { ex.printStackTrace(); }
                e.printStackTrace();
            }
        }
    }

}