package pieces;
import java.util.Scanner;

import engine.Move;
import engine.PiecePosition;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece{
    //pentru piece type folositi varianta in engleza al cuvantului e.g. pawn, queen, rook, knight, king, bishop
    public Pawn(boolean isWhite) {
        super(isWhite, "pawn");
    }


    @Override
    public Move[] getMoves(Piece[][] piecesArray){
        List<Move> currentMoves = new ArrayList<>();

        int x = getPostion().x;
        int y = getPostion().y;

        int moveNumber = hasMoved() ? 1:2;
        int incrementY = isWhite ? -1 : 1;

        for(int i=1; i<=moveNumber; i++){
            int newY = y + incrementY*i;

            if(i == 1){
                if(canCapture(x+1, newY, piecesArray)){
                    currentMoves.add(new Move(x+1, newY, this, true));
                }
                if(canCapture(x-1, newY, piecesArray)){
                    currentMoves.add(new Move(x-1, newY, this, true));
                }
            }

            if(canMove(x, newY, piecesArray)){
                currentMoves.add(new Move(x, newY, this));
            }else{
                break;
            }
        }

        return currentMoves.toArray(new Move[0]);
    }

}
