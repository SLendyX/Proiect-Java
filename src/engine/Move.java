package engine;

import pieces.Piece;

public record Move(PiecePosition piecePosition, Piece moveAuthor, boolean isCapture, boolean isEnpassant) {
    public Move(PiecePosition piecePosition, Piece moveAuthor, boolean isEnpassant) {
        this(piecePosition, moveAuthor, false, isEnpassant);
    }

    public Move(int x, int y, Piece moveAuthor) {
        this(new PiecePosition(x, y), moveAuthor, false, false);
    }

    public Move(int x, int y, Piece moveAuthor, boolean isCapture) {
        this(new PiecePosition(x, y), moveAuthor, isCapture, false);
    }


    public Move(int x, int y, Piece moveAuthor, boolean isCapture, boolean isEnpassant) {
        this(new PiecePosition(x, y), moveAuthor, isCapture, isEnpassant);
    }
}
