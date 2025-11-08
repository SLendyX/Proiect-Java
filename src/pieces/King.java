package pieces;

import engine.PiecePosition;

public class King extends Piece {
    public King(boolean isWhite) {
        super(isWhite, "king");
    }

    @Override
    public boolean canMove(int x, int y){
        return true;
    }

    @Override
    public PiecePosition[] getMoves(int x, int y){

        return null;
    }
}
