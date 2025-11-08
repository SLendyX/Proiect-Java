package pieces;

import engine.PiecePosition;

public class Knight extends Piece {
    public Knight(boolean isWhite) {
        super(isWhite, "knight");
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
