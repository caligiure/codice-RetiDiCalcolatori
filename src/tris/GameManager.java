package tris;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GameManager extends Thread {
    private final Player[] p;
    private final ObjectInputStream[] in;
    private final ObjectOutputStream[] out;
    private final Board board;

    public GameManager(Player p0, Player p1) {
        p = new Player[2];
        p[0] = p0;
        p[1] = p1;
        board = new Board();
        in = new ObjectInputStream[3];
        out =new ObjectOutputStream[3];
        System.out.println("Starting GameManager: "+p[0]+" vs "+p[1]);
        try{
            out[0] = new ObjectOutputStream(p0.getSock().getOutputStream());
            out[1] = new ObjectOutputStream(p1.getSock().getOutputStream());
            in[0] = new ObjectInputStream(p0.getSock().getInputStream());
            in[1] = new ObjectInputStream(p1.getSock().getInputStream());
        } catch (IOException e) {
            System.out.println("Error: "+e.getMessage());
        }
    } // constructor

    public void run() {
        try {
            System.out.println("Starting game: "+p[0]+" vs "+p[1]);
            boolean nextRound = true;
            int round = (int) Math.round(Math.random());
            while(nextRound) {
                for(int i=0; i<2; i++){
                    out[i].writeObject("READY");
                    out[i].writeObject(board);
                }
                out[(round+1)%2].writeObject("WAIT");
                move(round);
                if (board.checkWin()) {
                    winner(round);
                    loser((round + 1) % 2);
                    nextRound = false;
                }
                if (board.checkEndgame()) { // CONTROLLA SE LA SCACCHIERA E' PIENA
                    endgame();
                    nextRound = false;
                }
                round = (round + 1) % 2;
            } // while
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    } // run

    private void move(int i) throws IOException, ClassNotFoundException {
        out[i].writeObject("MOVE");
        boolean done = false;
        while(!done){
            Move m = (Move) in[i].readObject();
            done=true;
            try {
                board.makeMove(i, m.getX(), m.getY());
                out[i].writeObject("OK");
            } catch (IllegalArgumentException e) {
                out[i].writeObject("ERROR");
                out[i].writeObject(e);
                done=false;
            }
        } // while
    } // move

    private void winner(int i) throws IOException {
        out[i].writeObject("WINNER");
        out[i].writeObject(board);
    } // winner

    private void loser(int i) throws IOException {
        out[i].writeObject("LOSER");
        out[i].writeObject(board);
    } // loser

    private void endgame() throws IOException {
        for(int i=0; i<2; i++){
            out[i].writeObject("ENDGAME");
            out[i].writeObject(board);
        }
    } // endgame
}