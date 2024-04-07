package tris;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class TrisClient {
    private final Socket s;
    public TrisClient() {
        int serverPort = 8001; // su cui il server riceve le richieste di iniziare una partita
        try {
            InetAddress serverAddress = InetAddress.getByName("localhost");
            s = new Socket(serverAddress, serverPort);
            System.out.println("Connected to server");
            GamePlayer gp = new GamePlayer();
            gp.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    } // constructor

    class GamePlayer extends Thread {
        private final ObjectInputStream in;
        private final ObjectOutputStream out;

        public GamePlayer(){
            try {
                out = new ObjectOutputStream(s.getOutputStream());
                in = new ObjectInputStream(s.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } // constructor

        public void run(){
            boolean more = true;
            while(more){
                try{
                    String cmd = (String) in.readObject();
                    switch (cmd) {
                        case "READY":
                            System.out.println("BOARD:");
                            Board b = (Board) in.readObject();
                            System.out.println(b);
                            break;
                        case "WAIT":
                            System.out.println("Wait while your opponent makes a move");
                            break;
                        case "MOVE":
                            makeMove();
                            break;
                        case "WINNER":
                            System.out.println("YOU WIN");
                            Board b1 = (Board) in.readObject();
                            System.out.println(b1);
                            more = false;
                            break;
                        case "LOSER":
                            System.out.println("YOU LOSE");
                            Board b2 = (Board) in.readObject();
                            System.out.println(b2);
                            more = false;
                            break;
                        case "ENDGAME":
                            System.out.println("DRAW");
                            Board b3 = (Board) in.readObject();
                            System.out.println(b3);
                            more = false;
                            break;
                        default:
                            System.out.println("Error: command "+cmd+" is not supported.");
                    } // switch
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } // while
        } // run

        private void makeMove() throws IOException, ClassNotFoundException {
            boolean done = false;
            while (!done){
                System.out.println("It's your turnn to make a move");
                Scanner scan = new Scanner(System.in);
                System.out.print("insert row (0 / 1 / 2): ");
                String str = scan.nextLine();
                int x = Integer.parseInt(str);
                System.out.print("insert column (0 / 1 / 2): ");
                str = scan.nextLine();
                int y = Integer.parseInt(str);
                Move m = new Move(x, y);
                out.writeObject(m);
                String cmd = (String) in.readObject();
                switch (cmd) {
                    case "OK":
                        done=true;
                    case "ERROR":
                        String error = (String) in.readObject();
                        System.out.println(error);
                    default:
                        System.out.println("Error: command "+cmd+" is not supported.");
                } // switch
            } // while
        } // makeMove

    } // class

    public static void main(String[] args){
        new TrisClient();
    }

} // class