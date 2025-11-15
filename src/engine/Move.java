package engine;

import pieces.Piece;

public class Move {
    private final PiecePosition piecePosition;
    private final Piece moveAuthor;

    public Move(PiecePosition piecePosition, Piece moveAuthor){
        this.piecePosition = piecePosition;
        this.moveAuthor = moveAuthor;
    }

    public PiecePosition getPiecePosition() {
        return piecePosition;
    }

    public Piece getMoveAuthor() {
        return moveAuthor;
    }
}
