package pieces;

import engine.ChessEngine;
import engine.Move;
import engine.PiecePosition;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece{
    public Queen(boolean isWhite) {
        super(isWhite, "queen");
    }

    @Override
    public Move[] getMoves(Piece[][] piecesArray){
        List<Move> currentMoves = new ArrayList<>();

        int x = getPostion().x;
        int y = getPostion().y;

        int[] increments = {0,1,-1};

        for(int incrementX:increments){
            for(int incrementY:increments){
                if (incrementX == 0 && incrementY == 0) {
                    continue;
                }
                for(int i = 1; i < 8; i++) {
                    int newX = x + incrementX * i;
                    int newY = y + incrementY * i;

                    if (canMove(newX, newY, piecesArray)) {
                        currentMoves.add(new Move(newX, newY, this));
                    } else {
                        if(canCapture(newX, newY,piecesArray)) {
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
