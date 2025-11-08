package pieces;

import engine.PiecePosition;

public class Bishop extends Piece {
    public Bishop(boolean isWhite) {
        super(isWhite, "bishop");
    }

    //TODO: de creat logica pentru miscare
    @Override
    public boolean canMove(int x, int y){
        return true;
    }

    @Override
    public PiecePosition[] getMoves(int x, int y){

        return null;
    }
}
