package engine;

import board.BoardParameters;
import pieces.*;

import java.util.HashMap;
import java.util.Map;

public class ChessEngine {
    //    String defaultFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
    BoardParameters boardParam;
    String defaultFen = "8/8/8/4k3/3nQ2/8/PPPPPPPP/RNBQKBNR";

    public Piece[][] piecesArray;
    private Map<String, Move> movesArray;

    public ChessEngine(){
        this.piecesArray = null;
        this.movesArray = null;
    }

    public void setMovesArray(Piece piece){
        if(piece == null){
            this.movesArray = null;
            return;
        }

        Map<String, Move> movesMap = new HashMap<>();

        PiecePosition[] moves = piece.getMoves(piecesArray);
        if (moves == null) {
            this.movesArray = null;
            return;
        }

        for(PiecePosition move : moves){
            String key = move.chessCoordinate;
            movesMap.put(key, new Move(move, piece));
        }

        this.movesArray = movesMap;
    }

    public Map<String, Move> getMovesArray(){
        return this.movesArray;
    }

    public boolean doesMoveExist(int x, int y){
        if(this.movesArray == null){
            return false;
        }

        return this.movesArray.containsKey(getChessCoords(x, y));
    }

    public Move getMove(int x, int y){
        if(this.movesArray == null){
            return null;
        }

        return this.movesArray.get(getChessCoords(x, y));

    }

    public void swapSquares(Move move){
        int moveX = move.piecePosition.x;
        int moveY = move.piecePosition.y;

        int pieceX = move.moveAuthor.getPostion().x;
        int pieceY = move.moveAuthor.getPostion().y;

        piecesArray[moveY][moveX] = move.moveAuthor;
        piecesArray[pieceY][pieceX] = null;

        piecesArray[moveY][moveX].setPosition(moveX, moveY);
        if(!piecesArray[moveY][moveX].hasMoved()){
            piecesArray[moveY][moveX].setHasMoved(true);
        }
    }

    public void setBoardParams(BoardParameters boardParam){
        this.boardParam = boardParam;
    }

    public BoardParameters getBoardParams(){
        return this.boardParam;
    }


    public void instantiatePieceArray(String fen){
        createPieceArray(fen);
    }

    public void instantiatePieceArray(){
        createPieceArray(defaultFen);
    }

    private void createPieceArray(String defaultFen) {
        Piece[][] pieces = new Piece[8][8];

        int row=0, col=0;
        for(String fenRow : defaultFen.split("/")){
            for(String fenCol : fenRow.split("")){
                ///checks if text is a number
                if(fenCol.charAt(0) > '0' &&  fenCol.charAt(0) < '9'){
                    int x = Integer.parseInt(fenCol);
                    for(int j = 0; j < x; j++){
                        pieces[row][col++] = null;
                    }
                }else {
                    pieces[row][col] = instantiatePiece(fenCol);
                    pieces[row][col].setPosition(col, row, boardParam.isReversed);
                    col++;
                }
            }
            col= 0;
            row++;
        }

        this.piecesArray = pieces;
    }


    public Piece instantiatePiece(String pieceCharacter){
        char c = pieceCharacter.charAt(0);
        boolean isWhite = Character.isUpperCase(c);
        char lower = Character.toLowerCase(c);
        String lowerCaseChar = String.valueOf(lower);

        return switch (lowerCaseChar) {
            case "k" -> new King(isWhite);
            case "q" -> new Queen(isWhite);
            case "r" -> new Rook(isWhite);
            case "b" -> new Bishop(isWhite);
            case "n" -> new Knight(isWhite);
            case "p" -> new Pawn(isWhite);
            default -> null;
        };
    }


    public String getCurrentFen(){
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
