package ese4.horseBets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import static ese4.horseBets.BetClient.printInfo;

class Race extends Thread {
    private int raceID;
    private boolean active = false;
    private Date startTime;
    private Date startBetting;
    private LinkedList<BetServerMultiRace.Bet> bets;

    public Race(int raceID, Date startTime) {
        this.raceID = raceID;
        bets = new LinkedList<>();
        this.startTime = startTime;
        startBetting=calculateStartBetting();
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
            MulticastSocket ms = new MulticastSocket();
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
            throw new RuntimeException(e);
        }
    }
}