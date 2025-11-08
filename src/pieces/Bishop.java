package pieces;

import engine.PiecePosition;

import java.util.Stack;

public class Bishop extends Piece {
    public Bishop(boolean isWhite) {
        super(isWhite, "bishop");
    }

    //TODO: de creat logica pentru miscare
    @Override
    public boolean canMove(int x, int y, Piece[][] piecesArray){
        return true;
    }

    @Override
    public PiecePosition[] getMoves(int x, int y, Piece[][] piecesArray){
        return null;
    }
}
