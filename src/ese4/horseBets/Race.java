package ese4.horseBets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;


public class Race extends Thread {
    private final int raceID;
    private boolean active = false;
    private final Date startTime;
    private final Date startBetting;
    private final LinkedList<BetServerMultiRace.Bet> bets;

    public Race(int raceID, Date startTime) {
        this.raceID = raceID;
        bets = new LinkedList<>();
        this.startTime = startTime;
        startBetting=calculateStartBetting();
    }

    private static void printInfo(String s) {
        System.out.println(s);
    }

    int getRaceID() {
        return raceID;
    }

    boolean addBet(BetServerMultiRace.Bet bet) {
        if(!active) {
            bets.add(bet);
            return true;
        }
        else return false;
    }

    void activate() {
        active = true;
    }

    private Date calculateStartBetting(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);
        cal.add(Calendar.HOUR, -1);
        return cal.getTime();
    }

    Date getStartBetting() {
        return startBetting;
    }

    Date getStartTime() {
        return startTime;
    }

    boolean isActive() {
        return active;
    }

    public void run() {
        MulticastSocket ms = null;
        try {
            printInfo("Race "+getRaceID()+" started");
            Thread.sleep(10 * 1000);
            printInfo("Race "+getRaceID()+" ended");
            int winningHorse = (int) (Math.random()*12);
            printInfo("The winning horse of race " + getRaceID() + " is " + winningHorse);
            LinkedList<BetServerMultiRace.Bet> winners = new LinkedList<>();
            for (BetServerMultiRace.Bet b : bets) {
                if( b.horse == winningHorse ) {
                    winners.add(b);
                }
            }
            InetAddress addr = InetAddress.getByName("230.0.0.1");
            ms = new MulticastSocket();
            StringBuilder sb = new StringBuilder();
            for (BetServerMultiRace.Bet b : winners) {
                sb.append(b.address.toString()).append(' ').append(b.amount*12);
            }
            if(winners.isEmpty()){
                sb.append("No winners");
            }
            byte[] buf = sb.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, 8002);
            while (true) {
                ms.send(packet);
                Thread.sleep(10 * 1000); // wait before sending another multicast
            }
        } catch (InterruptedException | IOException e) {
            if(ms != null)
                ms.close();
            throw new RuntimeException(e);
        }
    }
}