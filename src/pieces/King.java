package pieces;

import engine.Move;
import engine.PiecePosition;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    public King(boolean isWhite) {
        super(isWhite, "king");
    }

    @Override
    public Move[] getMoves(Piece[][] piecesArray){
        List<Move> currentMoves = new ArrayList<>();

        int x = getPostion().x;
        int y = getPostion().y;

        int[] incrementsX = {0,1,-1};
        int[] incrementsY = {0,-1,1};

        for(int incrementX:incrementsX) {
            for (int incrementY : incrementsY) {
                if (incrementX == 0 && incrementY == 0) {
                    continue;
                }

                int newX = x + incrementX;
                int newY = y + incrementY;

                if (canMove(newX, newY, piecesArray)) {
                    currentMoves.add(new Move(newX, newY, this));
                }else if(canCapture(newX, newY, piecesArray)) {
                    currentMoves.add(new Move(newX, newY, this, true));
                }
            }
        }


        return currentMoves.toArray(new Move[0]);
    }
}
