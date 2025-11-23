package pieces;

import engine.Move;
import engine.PiecePosition;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {
    public Rook(boolean isWhite) {
        super(isWhite, "rook");
    }
    public Rook(boolean isWhite, PiecePosition position){
        super(isWhite, "rook");
        this.piecePosition = position;
    }

    @Override
    public Move[] getMoves(Piece[][] piecesArray){

        List<Move> currentMoves = new ArrayList<>();

        int x = getPostion().x;
        int y = getPostion().y;

        int[] increments = {0,1,-1};


        System.out.println("Rook");
        for(int incrementX:increments){
            for(int incrementY:increments){
                if((incrementX == 0 || incrementY == 0) && incrementX != incrementY)
                    for(int i = 1; i < 8; i++) {
                        int newX = x + incrementX * i;
                        int newY = y + incrementY * i;

                        if (canMove(newX, newY, piecesArray) ) {
                            System.out.println("can move");
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
