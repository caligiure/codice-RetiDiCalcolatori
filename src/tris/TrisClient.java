package tris;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import tris.GameManager.Cmd;

public class TrisClient {
    private final Socket s;
    public TrisClient() {
        int serverPort = 8001; // su cui il server riceve le richieste di iniziare una partita
        try {
            InetAddress serverAddress = InetAddress.getByName("localhost");
            s = new Socket(serverAddress, serverPort);
            System.out.println("Connected to server.");
            System.out.println("NOTE:\nEach box of the board is represented by the corresponding number.");
            System.out.println("Waiting for an opponent.");
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
                    Cmd c = (Cmd) in.readObject();
                    switch (c) {
                        case Cmd.READ_BOARD:
                            System.out.println("BOARD:");
                            String b = (String) in.readObject();
                            System.out.println(b);
                            break;
                        case Cmd.WAIT:
                            System.out.println("Wait while your opponent makes a move.");
                            break;
                        case Cmd.MOVE:
                            makeMove();
                            break;
                        case Cmd.WINNER:
                            System.out.println("YOU WIN");
                            String b1 = (String) in.readObject();
                            System.out.println(b1);
                            more = false;
                            break;
                        case Cmd.LOSER:
                            System.out.println("YOU LOSE");
                            String b2 = (String) in.readObject();
                            System.out.println(b2);
                            more = false;
                            break;
                        case Cmd.DRAW:
                            System.out.println("DRAW");
                            String b3 = (String) in.readObject();
                            System.out.println(b3);
                            more = false;
                            break;
                        default:
                            System.out.println("Command "+ c +" is not supported. (1)");
                    } // switch
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } // while
        } // run

        private void makeMove() throws IOException, ClassNotFoundException {
            boolean done = false;
            while (!done){
                System.out.println("It's your turn to make a move.");
                System.out.print("Select a box of the board to make a move: ");
                Scanner scan = new Scanner(System.in);
                String str = scan.nextLine();
                int k = Integer.parseInt(str);
                Move m = new Move(k);
                out.writeObject(m);
                Cmd c = (Cmd) in.readObject();
                switch (c) {
                    case Cmd.MOVE_DONE:
                        done=true;
                        break;
                    case Cmd.ERROR:
                        String error = (String) in.readObject();
                        System.out.println(error);
                        System.out.println("Try selecting another box.");
                        break;
                    default:
                        System.out.println("Command "+ c +" is not supported. (2)");
                        break;
                } // switch
            } // while
        } // makeMove

    } // class

    public static void main(String[] args){
        new TrisClient();
    }

} // class