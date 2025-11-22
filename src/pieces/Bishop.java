package pieces;

import engine.Move;
import engine.PiecePosition;

import java.util.ArrayList;
import java.util.List;


public class Bishop extends Piece {
    public Bishop(boolean isWhite) {
        super(isWhite, "bishop");
    }

    @Override
    public Move[] getMoves(Piece[][] piecesArray){
        List<Move> currentMoves = new ArrayList<>();

        int x = getPostion().x;
        int y = getPostion().y;

        int[] increments = {1,-1};

        for(int incrementX:increments){
            for(int incrementY:increments){
                for(int i = 1; i < 8; i++) {
                    int newX = x + incrementX * i;
                    int newY = y + incrementY * i;

                    if (canMove(newX, newY, piecesArray)) {
                        currentMoves.add(new Move(newX, newY, this));
                    } else {
                        if(canCapture(newX, newY, piecesArray)) {
                            currentMoves.add(new Move(newX, newY, this, true));
                        }
                        break;
                    }
                }
            }
        }



        return currentMoves.toArray(new Move[0]);
    }
}
