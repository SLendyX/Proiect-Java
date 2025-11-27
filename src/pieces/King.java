package pieces;

import engine.ChessEngine;
import engine.Move;

import java.util.ArrayList;
import java.util.List;


public class King extends Piece {
    public King(boolean isWhite) {
        super(isWhite, "king");
    }

    private boolean canCastle(int rookX, int y, Piece[][] board) {
        // 1. King must not have moved (you wanted this preserved)
        if (hasMoved())
            return false;

        Piece rookPiece = board[y][rookX];
        if (!(rookPiece instanceof Rook rook))
            return false;

        // 2. Rook must not have moved
        if (rook.hasMoved())
            return false;

        // 3. Determine direction (left or right)
        boolean isRightSide = rookX > piecePosition.x;
        int step = isRightSide ? 1 : -1;

        // 4. Check all squares between king and rook
        for (int i = piecePosition.x + step; i != rookX; i += step) {
            if (board[y][i] != null)
                return false;
        }

        return true;
    }


    @Override
    public Move[] getMoves(Piece[][] piecesArray){
        List<Move> currentMoves = new ArrayList<>();

        int x = getPostion().x;
        int y = getPostion().y;

        int[] increments = {0,1,-1};

        for(int incrementX :increments) {
            for (int incrementY : increments) {
                if (incrementX == 0 && incrementY == 0) continue;

                int newX = x + incrementX;
                int newY = y + incrementY;

                if (canMove(newX, newY, piecesArray)) {
                    currentMoves.add(new Move(newX, newY, this));
                }else if(canCapture(newX, newY, piecesArray)) {
                    currentMoves.add(new Move(newX, newY, this, true));
                }
            }
        }

        if(canCastle(0, y, piecesArray)){
            currentMoves.add(new Move(x-2, y, this, false, false, true, (Rook)piecesArray[y][0]));
        }

        if(canCastle(7, y, piecesArray)){
            currentMoves.add(new Move(x+2, y, this, false, false, true, (Rook)piecesArray[y][7]));
        }


        return currentMoves.toArray(new Move[0]);
    }
}
