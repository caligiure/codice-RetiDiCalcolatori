package tr2024_03_14;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Server {
    public static final String HOSTNAME = "agricoltura.dimes.unical.it";
    public static final int TCP_PORT_StatoSensore = 3000;
    public static final int TCP_PORT_RequestNotification = 4000;
    public static final int UDP_PORT_Notification = 4000;
    private final int ORA_INIZIO = 8;
    private final int ORA_FINE = 13;
    private final List<StatoSensore> statusRilevati = new LinkedList<>();
    private final Semaphore mutexStatusRilevati = new Semaphore(1);
    private final HashMap<InetAddress, Integer> clientRegistrati = new HashMap<>(); // <IP, ID_sensore>
    private final Semaphore mutexClientRegistrati = new Semaphore(1);

    public static void main(String[] args) {
        new Server();
    }

    private void printinfo(String s) { System.out.println(s); }

    private boolean checkHour() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour >= ORA_INIZIO && hour <= ORA_FINE;
    }

    private boolean checkUmid(StatoSensore s) throws InterruptedException {
        int somma = 0;
        int conta = 0;
        mutexStatusRilevati.acquire();
        for (StatoSensore s1 : statusRilevati) {
            if (s1.getID_sensore() == s.getID_sensore()) {
                somma += s1.getUmid();
                conta++;
            }
        }
        mutexStatusRilevati.release();
        double media = (double) somma / conta;
        double perc = media * 0.05;
        return s.getUmid() >= media - perc && s.getUmid() <= media + perc;
    }

    private boolean checkTemp(StatoSensore s) throws InterruptedException {
        int somma = 0;
        int conta = 0;
        mutexStatusRilevati.acquire();
        for (StatoSensore s1 : statusRilevati) {
            somma += s1.getTemp();
            conta++;
        }
        mutexStatusRilevati.release();
        double media = (double) somma / conta;
        double perc = media * 0.05;
        return s.getTemp() >= media - perc && s.getTemp() <= media + perc;
    }

    public Server() {
        printinfo("Starting Server...");
        new StatusReceiver().start();
        new NotificationRequestReceiver().start();
    }

    private class StatusReceiver extends Thread {
        @Override
        public void run() {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(TCP_PORT_StatoSensore);
                while (true) {
                    Socket client = ss.accept();
                    new StatusHandler(client).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    private class StatusHandler extends Thread {
        private final Socket client;
        public StatusHandler(Socket client) { this.client = client; }
        public void run() {
            try {
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                StatoSensore s = (StatoSensore) in.readObject();
                printinfo("Ricevuta richiesta: " + s);
                if (!checkHour()) {
                    out.println("Richiesta rifiutata: richiesta fuori dall'orario consentito");
                    printinfo("Rifiutata richiesta fuori dall'orario consentito");
                } else if (checkUmid(s) && checkTemp(s)) {
                    out.println("Richiesta rifiutata: valori già registrati");
                    printinfo("Rifiutata richiesta con valori già registrati");
                } else {
                    mutexStatusRilevati.acquire();
                    s.setNUM_stato(statusRilevati.size() + 1);
                    statusRilevati.add(s);
                    mutexStatusRilevati.release();
                    out.println("Richiesta accettata: ID=" + s.getNUM_stato());
                    printinfo("Registrata richiesta con ID=" + s.getNUM_stato());
                    new NotificationSender(s).start();
                }
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    if (client!=null) client.close();
                } catch (IOException ex) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class NotificationRequestReceiver extends Thread {
        @Override
        public void run() {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(TCP_PORT_RequestNotification);
                while (true) {
                    Socket client = ss.accept();
                    new NotificationRequestHandler(client).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    private class NotificationRequestHandler extends Thread {
        private final Socket client;
        public NotificationRequestHandler(Socket client) { this.client = client; }
        public void run() {
            try {
                PrintWriter out = new PrintWriter(client.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String req = in.readLine();
                printinfo("Ricevuta richiesta " + req); // "Notifica aggiornamenti al sensore: ID=1"
                StringTokenizer st = new StringTokenizer(req, "=", false);
                Integer ID_sensore = null;
                if (st.hasMoreTokens()) {
                    st.nextToken();
                    if (st.hasMoreTokens()) {
                        ID_sensore = Integer.parseInt(st.nextToken());
                    }
                }
                mutexClientRegistrati.acquire();
                clientRegistrati.put(client.getInetAddress(), ID_sensore);
                mutexClientRegistrati.release();
                out.println("Richiesta accettata: registrato al servizio di notifica");
                printinfo("Client " + client.getInetAddress() + " con sensore " + ID_sensore +
                        " registrato al servizio di notifica");
                client.close();
            } catch (Exception e) {
                if (client!=null) {
                    printinfo("Errore nella registrazione di client " + client.getInetAddress() );
                    try {
                        client.close();
                    } catch (IOException ex) {
                        e.printStackTrace();
                    }
                } else {
                    printinfo("Errore nella registrazione di un client" );
                }
                e.printStackTrace();
            }
        }

    }

    private class NotificationSender extends Thread {
        StatoSensore s;
        public NotificationSender(StatoSensore s) { this.s=s; }
        @Override
        public void run() {
            DatagramSocket ds = null;
            try {
                ds = new DatagramSocket();
                String msg = s.getID_sensore() + "#" + s.getNUM_stato() + "#" + s.getTemp() + "#" + s.getUmid();
                byte[] buf = msg.getBytes();
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                for( InetAddress ip : clientRegistrati.keySet() ) {
                    Integer ID_sensore = clientRegistrati.get(ip);
                    if ( ID_sensore == null || ID_sensore != s.getID_sensore() ) {
                        dp.setAddress(ip);
                        dp.setPort(UDP_PORT_Notification);
                        ds.send(dp);
                    }
                }
                printinfo("Notifica per aggiornamento da sensore "+s.getID_sensore()+" inviata");
                ds.close();
            } catch (IOException e) {
                printinfo("Errore nell'invio delle notifiche");
                if (ds!=null) ds.close();
                e.printStackTrace();
            }
        }
    }

}