package engine;

import pieces.Piece;
import pieces.Rook;

public record Move(PiecePosition piecePosition, Piece moveAuthor, boolean isCapture, boolean isEnpassant, boolean isCastle, Rook rook) {
//    public Move(PiecePosition piecePosition, Piece moveAuthor, boolean isEnpassant, boolean isCastle) {
//        this(piecePosition, moveAuthor, false, isEnpassant, isCastle);
//    }

    public Move(int x, int y, Piece moveAuthor) {
        this(new PiecePosition(x, y), moveAuthor, false, false, false, null);
    }

    public Move(int x, int y, Piece moveAuthor, boolean isCapture) {
        this(new PiecePosition(x, y), moveAuthor, isCapture, false, false, null);
    }


    public Move(int x, int y, Piece moveAuthor, boolean isCapture, boolean isEnpassant) {
        this(new PiecePosition(x, y), moveAuthor, isCapture, isEnpassant, false, null);
    }

    public Move(int x, int y, Piece moveAuthor, boolean isCapture, boolean isEnpassant, boolean isCastle, Rook rook) {
        this(new PiecePosition(x, y), moveAuthor, isCapture, isEnpassant, isCastle, rook);
    }
}
