package pieces;

import engine.PiecePosition;

public class Queen extends Piece{
    public Queen(boolean isWhite) {
        super(isWhite, "queen");
    }

    @Override
    public boolean canMove(int x, int y, Piece[][] piecesArray){
        return true;
    }

    @Override
    public PiecePosition[] getMoves(int x, int y, Piece[][] piecesArray){

        return null;
    }
}
