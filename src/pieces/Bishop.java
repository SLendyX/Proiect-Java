package pieces;

import engine.PiecePosition;

import java.util.ArrayList;
import java.util.List;


public class Bishop extends Piece {
    public Bishop(boolean isWhite) {
        super(isWhite, "bishop");
    }

    //TODO: de creat logica pentru miscare
    @Override
    public PiecePosition[] getMoves(Piece[][] piecesArray){
        List<PiecePosition> currentMoves = new ArrayList<>();

        int x = getPostion().x;
        int y = getPostion().y;

        int[] incrementsX = {1,-1};
        int[] incrementsY = {-1,1};

        for(int incrementX:incrementsX){
            for(int incrementY:incrementsY){
                for(int i = 1; i < 8; i++) {
                    if (canMove(x + incrementX * i, y + incrementY * i, piecesArray)) {
                        currentMoves.add(new PiecePosition(x + incrementX * i, y + incrementY * i));
                    } else {
                        break;
                    }
                }
            }
        }



        return currentMoves.toArray(new PiecePosition[0]);
    }
}
