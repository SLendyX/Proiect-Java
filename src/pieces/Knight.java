package pieces;

import engine.PiecePosition;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
    public Knight(boolean isWhite) {
        super(isWhite, "knight");
    }

    @Override
    public PiecePosition[] getMoves(Piece[][] piecesArray){
        int x = getPostion().x;
        int y = getPostion().y;

        List<PiecePosition> moves = new ArrayList<>();
        int[][] offsets = {
                {2,1},{2,-1},{-2,1},{-2,-1},
                {1,-2},{-1,-2},{-1,2},{1,2}
        };
        for (int[] offset : offsets) {
            int newX = x + offset[0];
            int newY = y + offset[1];

            if (canMove(newX, newY, piecesArray)) {
                moves.add(new PiecePosition(newX, newY));
            }
        }

        return moves.toArray(new PiecePosition[0]);
    }
}