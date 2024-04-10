package tris;

public class Board {
    private final int n;
    private final int m;
    private final char[][] mat;

    public Board(){
        n=3;
        m=3;
        mat = new char[n][m];
        reset();
    }

    public void reset() {
        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                mat[i][j] = '_';
            }
        }
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
                sb.append(" ").append(mat[i][j]).append(" ");
                if(j != m-1)
                    sb.append("|");
            }
            switch (i) {
                case 0: sb.append(" |  1 | 2 | 3 "); break;
                case 1: sb.append(" |  4 | 5 | 6 "); break;
                case 2: sb.append(" |  7 | 8 | 9 "); break;
                default: sb.append(" "); break;
            }
            if(i != n-1) {
                sb.append("\n---+---+--- | ---+---+---\n" );
            }
        }
        return sb.toString();
    }

    public boolean makeMove(char mark, int x, int y) throws IllegalArgumentException {
        if(x<0 || x>=n)
            throw new IllegalArgumentException("Illegal x argument");
        else if(y<0 ||y>=m)
            throw new IllegalArgumentException("Illegal y argument");
        else if(mark!='X' && mark!='O')
            throw new IllegalArgumentException("Illegal mark argument");
        else if(mat[x][y]=='O' || mat[x][y]=='X')
            throw new IllegalArgumentException("This box is already marked");
        else {
            mat[x][y]=mark;
            return true;
        }
    }

    public boolean checkWin() {
        for(int i=0; i<n; i++) {
            if(mat[i][0]==mat[i][1] && mat[i][1]==mat[i][2] && mat[i][2]!='_')
                return true;
            if(mat[0][i]==mat[1][i] && mat[1][i]==mat[2][i] && mat[2][i]!='_')
                return true;
        }
        return mat[0][0] == mat[1][1] && mat[1][1] == mat[2][2] && mat[2][2] != '_';
    }

    public boolean checkDraw() {
        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                if(mat[i][j]=='_')
                    return false;
            }
        }
        return true;
    }

    public static void main(String[] args){
        Board b = new Board();
        b.makeMove('X',0,0);
        b.makeMove('X',1,1);
        b.makeMove('X',2,2);
        System.out.println(b);
        System.out.println(b.checkWin());
        b.reset();
        b.makeMove('X',0,2);
        b.makeMove('O',1,1);
        b.makeMove('X',2,0);
        System.out.println(b);
        System.out.println(b.checkWin());
        b.reset();
        b.makeMove('X',2,0);
        b.makeMove('X',2,1);
        b.makeMove('X',2,2);
        System.out.println(b);
        System.out.println(b.checkWin());
        b.reset();
        for(int i=0;i<b.getN();i++){
            for(int j=0;j<b.getM();j++){
                long p = Math.round(Math.random());
                if(p==0)
                    b.makeMove('O', i, j);
                else
                    b.makeMove('X', i, j);
            }
        }
        System.out.println(b);
        System.out.println(b.checkWin());
        System.out.println(b.checkDraw());

    }

}