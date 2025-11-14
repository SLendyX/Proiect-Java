package engine;

import pieces.Piece;

public class Move {
    public PiecePosition piecePosition;
    public Piece moveAuthor;

    public Move(PiecePosition piecePosition, Piece moveAuthor){
        this.piecePosition = piecePosition;
        this.moveAuthor = moveAuthor;
    }

}
