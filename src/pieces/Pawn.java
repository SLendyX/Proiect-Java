package pieces;

import engine.PiecePosition;

public class Pawn extends Piece{
    //pentru piece type folositi varianta in engleza al cuvantului e.g. pawn, queen, rook, knight, king, bishop
    public Pawn(boolean isWhite) {
        super(isWhite, "pawn");
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
