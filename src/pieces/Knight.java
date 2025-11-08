package pieces;

import engine.PiecePosition;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
    public Knight(boolean isWhite) {
        super(isWhite, "knight");
    }

    //TODO: de creat logica pentru miscare
    @Override
    public boolean canMove(int x, int y, Piece[][] piecesArray){
        if(x>=0 && x<8  && y>=0 && y<8){
            return piecesArray[x][y] == null;
        }
        return false;
    }

    @Override
    public PiecePosition[] getMoves(int x, int y, Piece[][] piecesArray){
        List<PiecePosition> moves = new ArrayList<>();
        int[][] offsets = {
                {2,1},{2,-1},{-2,1},{-2,-1},
                {1,-2},{-1,-2},{-1,2},{1,2}
        };
        for (int[] offset : offsets) {
            int newX = x + offset[0];
            int newY = y + offset[1];

            if (canMove(newX, newY, piecesArray)) {
                //adaugam pozitia disponibila in lista
                moves.add(new PiecePosition(newX, newY));
            }
        }

        return moves.toArray(new PiecePosition[0]);
    }
}