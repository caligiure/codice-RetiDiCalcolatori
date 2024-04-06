package tris;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GameManager extends Thread {
    private final Player[] p;
    private final ObjectInputStream[] in;
    private final ObjectOutputStream[] out;
    private final Board board;

    public GameManager(Player p1, Player p2) {
        p = new Player[3];
        p[1] = p1;
        p[2] = p2;
        board = new Board();
        in = new ObjectInputStream[3];
        out =new ObjectOutputStream[3];
        try{
            in[1] = new ObjectInputStream(p1.getSock().getInputStream());
            in[2] = new ObjectInputStream(p2.getSock().getInputStream());
            out[1] = new ObjectOutputStream(p1.getSock().getOutputStream());
            out[2] = new ObjectOutputStream(p2.getSock().getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            System.out.println("Starting game: "+p[1]+" vs "+p[2]);
            out[1].writeObject("START");
            out[2].writeObject("START");
            boolean nextRound = true;
            boolean round1 = true; // PLAYER 1 gioca sempre per primo
            while(nextRound) {
                for(int i=1; i<=2; i++){
                    out[i].writeObject("READY");
                    out[i].writeObject(board);
                }
                if(round1){
                    out[2].writeObject("WAIT");
                    boolean win1 = move(1);
                    if(win1){
                        winner(1);
                        loser(2);
                        nextRound=false;
                    }
                    // CONTROLLA SE LA SCACCHIERA E' PIENA
                    if(!win1){
                        move(2);
                        boolean win2 = board.checkWin();
                        if(win2){
                            winner(2);
                            loser(1);
                            nextRound=false;
                        }
                    }
                } // round1
                else {

                }
                round1 = !round1;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean move(int i) throws IOException, ClassNotFoundException {
        out[i].writeObject("MOVE");
        boolean done = false;
        while(!done){
            Move m = (Move) in[i].readObject();
            done=true;
            try {
                board.makeMove(i, m.getX(), m.getY());
            } catch (IllegalArgumentException e) {
                out[i].writeObject("ERROR");
                out[i].writeObject(e);
                done=false;
            }
        } // while
        return board.checkWin();
    } // move

    private void winner(int i) throws IOException {
        out[i].writeObject("ENDGAME");
        out[i].writeObject("WINNER");
        out[i].writeObject(board);
    }

    private void loser(int i) throws IOException {
        out[i].writeObject("ENDGAME");
        out[i].writeObject("LOSER");
        out[i].writeObject(board);
    }
}