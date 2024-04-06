package tris;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GameManager extends Thread {
    private final Player p1, p2;
    private final ObjectInputStream in1, in2;
    private final ObjectOutputStream out1, out2;
    private final Board board;

    public GameManager(Player p1, Player p2) {
        this.p1 = p1;
        this.p2 = p2;
        board = new Board();
        try{
            in1 = new ObjectInputStream(p1.getSock().getInputStream());
            in2 = new ObjectInputStream(p2.getSock().getInputStream());
            out1 = new ObjectOutputStream(p1.getSock().getOutputStream());
            out2 = new ObjectOutputStream(p2.getSock().getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            System.out.println("Starting game: "+p1+" vs "+p2);
            out1.writeObject("START");
            out2.writeObject("START");
            boolean nextRound = true;
            boolean round1 = true; // PLAYER 1 gioca sempre per primo
            while(nextRound) {
                out1.writeObject("READ");
                out1.writeObject(board);
                out2.writeObject("READ");
                out2.writeObject(board);
                if(round1){
                    out1.writeObject("MOVE");
                    out2.writeObject("WAIT");
                    Move m1 = (Move) in1.readObject();
                    board.makeMove(1, m1.getX(), m1.getY());
                    if(board.checkWin()){

                    }
                    out2.writeObject("MOVE");
                    out1.writeObject("WAIT");
                    Move m2 = (Move) in2.readObject();
                    board.makeMove(2, m2.getX(), m2.getY());
                    if(board.checkWin()){

                    }
                } else {

                }
                round1 = !round1;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
