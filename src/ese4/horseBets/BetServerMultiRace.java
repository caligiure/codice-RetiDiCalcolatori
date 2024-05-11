package ese4.horseBets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class BetServerMultiRace {
    int SERVER_TCP_PORT = 8001;
    private final Starter starter;
    private final GameManager gameManager;

    public BetServerMultiRace(LinkedList<Race> races) {
        new Dispatcher().start();
        starter = new Starter();
        starter.start();
        gameManager = new GameManager(races);
        gameManager.start();
    }

    public static void main(String[] args) {
        LinkedList<Race> races = new LinkedList<>();
        Calendar startTime = Calendar.getInstance();
        startTime.add(Calendar.MINUTE, 2);
        Race race1 = new Race(1, startTime.getTime());
        Race race2 = new Race(2, startTime.getTime());
        races.add(race1);
        races.add(race2);
        new BetServerMultiRace(races);
    }

    private static void printInfo(String message) {
        System.out.println(message);
    }
    private static void printError(String message, Exception e) {
        System.err.println(message + "\n" + e);
    }

    class Dispatcher extends Thread {
        public void run() {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(SERVER_TCP_PORT);
                printInfo("Accepting connections on port " + SERVER_TCP_PORT);
                while (true) {
                    Socket client = ss.accept();
                    starter.addClient(client);
                    printInfo("Accepted client "+client.getInetAddress());
                }
            } catch (IOException e) {
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (IOException ex) {
                        printError("Error closing server socket", ex);
                    }
                }
                printError("Error creating server socket", e);
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
                        printError("Starter has been interrupted", e);
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
            PrintWriter out = null;
            try {
                BufferedReader in= new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);
                String races = gameManager.getRaces();
                out.println(races); // 1. send races
                printInfo("Sent races to "+client.getInetAddress());
                String response = in.readLine(); //2. get bet
                printInfo("Received bet from "+client.getInetAddress()+": "+response);
                Bet bet = new Bet(response, client.getInetAddress());
                if(gameManager.placeBet(bet))
                    out.println("Bet placed correctly"); // 3. send outcome
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

    static class GameManager extends Thread {
        private final LinkedList<Race> races;

        public GameManager(LinkedList<Race> races) {
            this.races = races;
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
            while(true) {
                for(Race race : races) {
                    Date now = Calendar.getInstance().getTime();
                    if(race.getStartBetting().before(now) && race.getStartBetting().after(now)) {
                        race.activate();
                        race.start();
                        printInfo("RACE "+race.getRaceID()+" started");
                    }
                }
            }
        }
    }

}