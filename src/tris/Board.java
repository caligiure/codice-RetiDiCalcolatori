package tris;

import java.io.Serializable;

public class Board implements Serializable {
    private final int n;
    private final int m;
    private final int[][] mat;

    public Board(){
        n=3;
        m=3;
        mat = new int[n][m];
    }

    @SuppressWarnings("unused")
    public Board(int n, int m) {
        this.n = n;
        this.m = m;
        mat = new int[n][m];
    }

    public int getN() {
        return n;
    }

    public int getM() {
        return m;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<n; i++){
            for(int j=0; j<m; j++){
                if(mat[i][j]==1)
                    sb.append("X").append(" ");
                else if(mat[i][j]==2)
                    sb.append("O").append(" ");
                else
                    sb.append("_").append(" ");
            }
            if(i!=n-1)
                sb.append("\n");
        }
        return sb.toString();
    }

    public void makeMove(int p, int x, int y) throws IllegalArgumentException {
        if(x<0 || x>=n)
            throw new IllegalArgumentException("Illegal x argument");
        if(y<0 ||y>=m)
            throw new IllegalArgumentException("Illegal y argument");
        if(p!=1 && p!=2)
            throw new IllegalArgumentException("Illegal p argument");
        if(mat[x][y]!=0)
            throw new IllegalArgumentException("This box is already marked");
        mat[x][y]=p;
    }

    public boolean checkWin() {
        for(int i=1; i<n-1; i++){
            for(int j=1; j<m-1; j++){
                int ul = mat[i-1][j-1]; // up-left
                int ur = mat[i-1][j+1]; // up-right
                int cc = mat[i][j];     // center
                int uc = mat[i-1][j];   // up-center
                int dc = mat[i+1][j];   // down-center
                int lc = mat[i][j-1];   // left-center
                int rc = mat[i][j+1];   // right-center
                int dl = mat[i+1][j-1]; // down-left
                int dr = mat[i+1][j+1]; // down-right
                boolean tris = ( ul == uc && ul == ur && ul != 0 )  // tris orizzontale superiore
                        ||
                                ( ul != 0 && ul == lc && ul == dl)  // tris verticale sinistro
                        ||
                                ( dr != 0 && dr == dl && dr == dc ) // tris orizzonatle inferiore
                        ||
                                ( dr != 0 && dr == ur && dr == rc ) // tris verticale destro
                        ||
                                ( cc != 0 && cc == ul && cc == dr)  // tris diagonale principale
                        ||
                                ( cc != 0 && cc == ur && cc == dl)  // tris diagonale secondaria
                        ||
                                ( cc != 0 && cc == lc && cc == rc)  // tris orizzontale centrale
                        ||
                                (cc != 0 && cc == uc && cc == dc)   // tris verticale centrale
                ;
                if(tris)
                    return true;
            }
        }
        return false;
    }

    public static void main(String[] args){
        Board b = new Board();
        b.makeMove(1,0,0);
        b.makeMove(1,1,1);
        b.makeMove(1,2,2);
        System.out.println(b);
        System.out.println(b.checkWin());
        b.makeMove(1,0,0);
        b.makeMove(2,1,1);
        b.makeMove(1,2,2);
        System.out.println(b);
        System.out.println(b.checkWin());
        b.makeMove(1,2,0);
        b.makeMove(1,2,1);
        b.makeMove(1,2,2);
        System.out.println(b);
        System.out.println(b.checkWin());
    }
}