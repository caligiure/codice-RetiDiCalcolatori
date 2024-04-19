package ese4.ludopatia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class BetServer {
    // Le scommesse sono ricevute, tramite il protocollo TCP, sulla porta 8001 del server
    public static final int SERVER_PORT = 8001;
    private boolean accepting = true;
    private final LinkedList<Bet> bets = new LinkedList<>();

    private void printInfo(String s){
        System.out.println(s);
    }

    public void startServer() {
        printInfo("Server started");
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            BetManager bm = new BetManager(serverSocket);
            bm.start();
            printInfo("BetManager started");
            int timeout = 10 * 1000;
            Thread.sleep(timeout);
            accepting = false;
            printInfo("Bets closed");
            int winningHorse = (int) (Math.random()*12);
            printInfo("The winning horse is " + winningHorse);
            LinkedList<Bet> winners = new LinkedList<>();
            for (Bet b : bets) {
                if( b.horse == winningHorse ) {
                    winners.add(b);
                }
            }
            new sendWinners(winners).start();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    class BetManager extends Thread {
        ServerSocket serverSocket;

        public BetManager(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run() {
            while (true) {
                printInfo("Accepting next client");
                try {
                    Socket client = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    if(accepting){
                        out.println("Place your bet");
                        String s = in.readLine();
                        Bet b = new Bet(s, client.getPort(), client.getInetAddress());
                        bets.add(b);
                    } else {
                        out.println("Bets closed");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }

    class sendWinners extends Thread {
        List<Bet> winners;
        public sendWinners(List<Bet> winners) {
            this.winners = winners;
        }

        public void run() {
            // Tale comunicazione avviene tramite l’invio, sul gruppo multicast ”230.0.0.1” e sulla porta “8002”
            //cui sono collegati tutti i client, di coppie di stringhe “<ip_vincitore> <somma>”
            try {
                InetAddress addr = InetAddress.getByName("230.0.0.1");
                MulticastSocket ms = new MulticastSocket();
                StringBuilder sb = new StringBuilder();
                for (Bet b : winners) {
                    sb.append(b.address.toString()).append(' ').append(b.port).append(' ').append(b.money*12);
                }
                if(winners.isEmpty()){
                    sb.append("No winners");
                }
                byte[] buf = sb.toString().getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, 8002);
                ms.send(packet);
                ms.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class Bet {
        InetAddress address;
        int port;
        int horse;
        int money;
        
        public Bet(String bet, int port, InetAddress address) {
            this.port = port;
            this.address = address;
            StringTokenizer st = new StringTokenizer(bet);
            if(st.hasMoreTokens()){
                horse = Integer.parseInt(st.nextToken());
            } else {
                horse = 0;
            }
            if(st.hasMoreTokens()){
                money = Integer.parseInt(st.nextToken());
            } else {
                money = 0;
            }
        }
    }

    public static void main(String[] args) {
        new BetServer().startServer();
    }

}
