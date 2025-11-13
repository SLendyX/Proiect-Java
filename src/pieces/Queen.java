package pieces;

import engine.PiecePosition;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece{
    public Queen(boolean isWhite) {
        super(isWhite, "queen");
    }

    @Override
    public boolean canMove(int x, int y, Piece[][] piecesArray){
        if(x < 0 || x>7 || y < 0 || y>7){
            return false;
        }

        return piecesArray[y][x] == null;
    }

    @Override
    public PiecePosition[] getMoves(Piece[][] piecesArray){
        List<PiecePosition> currentMoves = new ArrayList<>();

        int x = getPostion().x;
        int y = getPostion().y;

        int[] incrementsX = {0,1,-1};
        int[] incrementsY = {0,-1,1};

        for(int i = 1; i < 8; i++) {
            for(int incrementX:incrementsX){
                for(int incrementY:incrementsY){
                    if(incrementX == 0 && incrementY == 0) {
                        continue;
                    }

                    if(canMove(x+incrementX*i,y+incrementY*i,piecesArray)){
                        currentMoves.add(new PiecePosition(x+incrementX*i,y+incrementY*i));
                    }
                }
            }
        }

        return currentMoves.toArray(new PiecePosition[0]);
    }
}
