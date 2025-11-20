package engine;

import pieces.Piece;

public class Move {
    private final PiecePosition piecePosition;
    private final Piece moveAuthor;
    private final boolean isCapture;

    public Move(PiecePosition piecePosition, Piece moveAuthor){
        this.piecePosition = piecePosition;
        this.moveAuthor = moveAuthor;
        this.isCapture = false;
    }

    public Move(PiecePosition piecePosition, Piece moveAuthor, boolean isCapture) {
        this.piecePosition = piecePosition;
        this.moveAuthor = moveAuthor;
        this.isCapture = isCapture;
    }

    public Move(int x, int y, Piece moveAuthor) {
        this.piecePosition = new PiecePosition(x,y);
        this.moveAuthor = moveAuthor;
        this.isCapture = false;
    }

    public Move(int x, int y, Piece moveAuthor, boolean isCapture) {
        this.piecePosition = new PiecePosition(x,y);
        this.moveAuthor = moveAuthor;
        this.isCapture = isCapture;
    }

    public PiecePosition getPiecePosition() {
        return piecePosition;
    }

    public Piece getMoveAuthor() {
        return moveAuthor;
    }

    public boolean isCapture() {
        return isCapture;
    }
}
