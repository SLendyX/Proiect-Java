package pieces;

import engine.PiecePosition;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    public King(boolean isWhite) {
        super(isWhite, "king");
    }

    @Override
    public PiecePosition[] getMoves(Piece[][] piecesArray){
        List<PiecePosition> currentMoves = new ArrayList<>();

        int x = getPostion().x;
        int y = getPostion().y;

        int[] incrementsX = {0,1,-1};
        int[] incrementsY = {0,-1,1};

        for(int incrementX:incrementsX) {
            for (int incrementY : incrementsY) {
                if (incrementX == 0 && incrementY == 0) {
                    continue;
                }
                if (canMove(x + incrementX, y + incrementY, piecesArray)) {
                    currentMoves.add(new PiecePosition(x + incrementX, y + incrementY));
                }
            }
        }


        return currentMoves.toArray(new PiecePosition[0]);
    }
}
