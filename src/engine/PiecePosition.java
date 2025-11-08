package engine;

public class PiecePosition {
    public String chessCoordinate;
    public int x;
    public int y;

    //new PiecePosition(x,y);

    public PiecePosition(int x, int y)
    {
        this.chessCoordinate=new ChessEngine().getChessCoords(x,y);
        this.x=x;
        this.y=y;
    }

    public PiecePosition(int x, int y, boolean isReversed){
        this.chessCoordinate=new ChessEngine().getChessCoords(x,y, isReversed);
        this.x=x;
        this.y=y;
    }
}
