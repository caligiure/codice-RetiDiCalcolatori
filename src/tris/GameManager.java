package tris;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class GameManager extends Thread {
    private final Player[] p;
    private final ObjectInputStream[] in;
    private final ObjectOutputStream[] out;
    private final Board board;

    public enum Cmd implements Serializable {
        READ_BOARD, WAIT, MOVE, WINNER, LOSER, DRAW, MOVE_DONE, ERROR
    }

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
            int turn = (int) Math.round(Math.random());
            char mark = (turn==0)? 'O' : 'X';
            while(nextRound) {
                for(int i=0; i<2; i++){
                    out[i].writeObject(Cmd.READ_BOARD);
                    out[i].writeObject(board.toString());
                }
                out[(turn+1)%2].writeObject(Cmd.WAIT);
                getMove(turn, mark);
                if (board.checkWin()) {
                    winner(turn);
                    loser((turn + 1) % 2);
                    nextRound = false;
                }
                else if (board.checkDraw()) {
                    draw();
                    nextRound = false;
                }
                turn = (turn + 1) % 2;
                mark = (turn==0)? 'O' : 'X';
            } // while
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    } // run

    private void getMove(int turn, char mark) throws IOException, ClassNotFoundException {
        out[turn].writeObject(Cmd.MOVE);
        boolean move_done = false;
        while(!move_done){
            Move m = (Move) in[turn].readObject();
            try {
                move_done = board.makeMove(mark, m.getX(), m.getY());
                out[turn].writeObject(Cmd.MOVE_DONE);
            } catch (IllegalArgumentException e) {
                out[turn].writeObject(Cmd.ERROR);
                out[turn].writeObject(e.getMessage());
            }
        } // while
    } // move

    private void winner(int i) throws IOException {
        out[i].writeObject(Cmd.WINNER);
        out[i].writeObject(board.toString());
    } // winner

    private void loser(int i) throws IOException {
        out[i].writeObject(Cmd.LOSER);
        out[i].writeObject(board.toString());
    } // loser

    private void draw() throws IOException {
        for(int i=0; i<2; i++){
            out[i].writeObject(Cmd.DRAW);
            out[i].writeObject(board.toString());
        }
    } // endgame

}