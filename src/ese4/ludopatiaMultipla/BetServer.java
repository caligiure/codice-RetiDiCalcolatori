package ese4.ludopatiaMultipla;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

public class BetServer {
    int SERVER_TCP_PORT = 8001;
    private Dispatcher dispatcher;
    private Starter starter;
    private GameManager gameManager;

    public BetServer(LinkedList<Race> races) {
        new Dispatcher().start();
        new Starter().start();
        new GameManager(races).start();
    }

    void printInfo(String s) {
        System.out.println(s);
    }

    class Dispatcher extends Thread {
        public void run() {
            try {
                ServerSocket ss = new ServerSocket(SERVER_TCP_PORT);
                while (true) {
                    Socket client = ss.accept();
                    starter.addClient(client);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class Starter extends Thread {
        private final LinkedList<Socket> clientList = new LinkedList<>();

        void addClient(Socket client) {
            clientList.add(client);
        }

        public void run() {
            int i=0;
            while (true) {
                while(i>=clientList.size()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                Socket client = clientList.getLast();
                new ClientManager(client).start();
                i++;
            }
        }
    }

    class ClientManager extends Thread {
        Socket client;

        public ClientManager(Socket client) {
            this.client=client;
        }

        public void run() {
            BufferedReader in = null;
            PrintWriter out = null;
            try {
                in= new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);
                String races = gameManager.getRaces();
                out.println(races);
                String response = in.readLine();
                Bet bet = new Bet(response, client.getInetAddress());
                if(gameManager.placeBet(bet))
                    out.println("Bet placed correctly");
                else
                    out.println("Bet can't be placed");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                if(out!=null) {
                    out.println("Can't place bet: "+e.getMessage());
                }
            }
        }
    }

    static class Bet {
        InetAddress address;
        int race;
        int horse;
        int amount;

        public Bet(String bet, InetAddress address) {
            this.address = address;
            String[] parts = bet.split(" ");
            // race horse amount
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid bet");
            }
            race = Integer.parseInt(parts[0]);
            horse = Integer.parseInt(parts[1]);
            amount = Integer.parseInt(parts[2]);
        }
    }

    class GameManager extends Thread {
        private LinkedList<Race> races;

        public GameManager(LinkedList<Race> races) {
            races=new LinkedList<>();
        }

        boolean placeBet(Bet bet) {
            for(Race race : races) {
                if(race.getRaceID() == bet.race) {
                    return race.addBet(bet);
                }
            }
            return false;
        }

        String getRaces(){
            StringBuilder sb = new StringBuilder();
            for(Race race : races) {
                if(race.isActive()){
                    sb.append("- ").append(race.getRaceID());
                    sb.append(": startTime ").append(race.getStartTime()).append("\n");
                }
            }
            return sb.toString();
        }

        public void run() {
            Iterator<Race> it = races.iterator();
            while(true) {
                for(Race race : races) {
                    Date now = Calendar.getInstance().getTime();
                    if(race.getStartBetting().before(now) && race.getStartBetting().after(now)) {
                        race.activate();
                        race.start();
                    }
                }
            }
        }
    }
}
