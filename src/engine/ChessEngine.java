package engine;

import pieces.Piece;

public class ChessEngine {
    public String getCurrentFen(Piece[][] piecesArray){
        StringBuilder fen = new StringBuilder();

        int i=0;
        int emptySpace;
        for(Piece[] row : piecesArray){
            emptySpace = 0;
            for(Piece p : row){
                if(p == null) {
                    emptySpace++;
                }
                else{
                    if(emptySpace > 0){
                        fen.append(Integer.toString(emptySpace));
                        emptySpace = 0;
                    }
                    fen.append(p.getFenChar());
                }
            }
            if(emptySpace > 0)
                fen.append(Integer.toString(emptySpace));

            if(i++ < 7)
                fen.append("/");
        }

        return fen.toString();
    }

    public String getChessCoords(int x, int y){
        String cols = "abcdefgh";
        String rows = "87654321";

        return String.valueOf(cols.charAt(x)) +
                rows.charAt(y);
    }

    public String getChessCoords(int x, int y, boolean isReversed){
        String cols;
        String rows;
        if(isReversed){
            cols = "hgfedcba";
            rows = "12345678";
        }else{
            cols = "abcdefgh";
            rows = "87654321";
        }

        return String.valueOf(cols.charAt(x)) +
                rows.charAt(y);
    }
}
