package pieces;

import engine.PiecePosition;

public class Rook extends Piece {
    public Rook(boolean isWhite) {
        super(isWhite, "rook");
    }

    //TODO: pice move logic
    @Override
    public boolean canMove(int x, int y, Piece[][] piecesArray){
        return true;
    }

    @Override
    public PiecePosition[] getMoves(Piece[][] piecesArray){

        return null;
    }
}
