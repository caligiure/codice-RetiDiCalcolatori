package ese4.horseBets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Scanner;

public class BetClient {

    private InetAddress SERVER_IP;
    static final int SERVER_TCP_PORT = 8001;
    static final String multicastGroup = "230.0.0.1";
    static final int SERVER_MULTICAST_PORT = 8002;

    BetClient() {
        try {
            SERVER_IP = InetAddress.getLocalHost();
        } catch (IOException e) { printError("Couldn't get IP", e); }
        entryPoint();
    }

    private static void printInfo(String message) {
        System.out.println(message);
    }
    private static void printError(String message, Exception e) {
        System.err.println(message + "\n" + e);
    }

    void entryPoint() {
        BufferedReader in = null;
        PrintWriter pw = null;
        Socket server;
        try {
            printInfo("Trying to connect to server on port " + SERVER_TCP_PORT);
            server = new Socket(SERVER_IP, SERVER_TCP_PORT);
            printInfo("Connected to server");
            in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            String line = in.readLine(); // 1. get races
            printInfo(line);
            Scanner user = new Scanner(System.in);
            String bet = user.nextLine();
            pw = new PrintWriter(server.getOutputStream(), true);
            pw.println(bet); // 2. send bet
            printInfo("Bet sent!");
            line = in.readLine(); // 3. get outcome
            printInfo(line);
        } catch (IOException e) {
            printError("Something failed: ", e);
        } finally {
            try {
                if (in != null) in.close();
                if (pw != null) pw.close();
            } catch (IOException e) {
                printError("Couldn't shut down pipe gracefully", e);
            }
        }
        printInfo("Wait for the bet's results");
        MulticastSocket ms;
        try {
            ms = new MulticastSocket(SERVER_MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(multicastGroup);
            ms.joinGroup(group);
            printInfo("Joined group" + group);
            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            ms.receive(packet);
            buf = packet.getData();
            String dString = new String(buf).trim();
            System.out.println(dString);
            ms.leaveGroup(group);
        } catch (IOException e) {
            System.err.println("Something multicasty happened");
        }

    }


    public static void main(String[] args) { new BetClient(); }
}