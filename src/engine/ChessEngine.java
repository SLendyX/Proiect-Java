package engine;

import board.BoardParameters;
import pieces.*;

import java.util.HashMap;
import java.util.Map;

public class ChessEngine {
    //    String defaultFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
    BoardParameters boardParam;
    String defaultFen = "rnbqkbnr/pppppppp/8/8/8/8/PPP1PPPP/RNBQKBNR";
    boolean isPromoting = false;
    boolean turn = true;
    Piece promotingPawn;



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

        Move[] moves = piece.getMoves(piecesArray);
        if (moves == null) {
            this.movesArray = null;
            return;
        }

        for(Move move : moves){
            String key = move.getPiecePosition().chessCoordinate;
            movesMap.put(key, move);
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
        int moveX = move.getPiecePosition().x;
        int moveY = move.getPiecePosition().y;

        int pieceX = move.getMoveAuthor().getPostion().x;
        int pieceY = move.getMoveAuthor().getPostion().y;

        piecesArray[moveY][moveX] = move.getMoveAuthor();
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
                        fen.append(emptySpace);
                        emptySpace = 0;
                    }
                    fen.append(p.getFenChar());
                }
            }
            if(emptySpace > 0)
                fen.append(emptySpace);

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

    public boolean canPromote(Piece pawn){
        int posY = pawn.getPostion().y;
        boolean isWhite = pawn.isWhite();

        if(isWhite && (posY == 1)){
            return true;
        }

        return !isWhite && (posY == 6);
    }

    public void switchPiece(Piece pawn, int choice) {
        if (canPromote(pawn)) {
            int posY = pawn.getPostion().y;
            int posX = pawn.getPostion().x;
            boolean isWhite = pawn.isWhite();




            switch (choice) {
                case 0:
                    piecesArray[posY][posX] = new Queen(isWhite);
                    piecesArray[posY][posX].setPosition(posX, posY);
                    break;
                case 3:
                    piecesArray[posY][posX] = new Knight(isWhite);
                    piecesArray[posY][posX].setPosition(posX, posY);
                    break;
                case 1:
                    piecesArray[posY][posX] = new Rook(isWhite);
                    piecesArray[posY][posX].setPosition(posX, posY);
                    break;
                case 2:
                    piecesArray[posY][posX] = new Bishop(isWhite);
                    piecesArray[posY][posX].setPosition(posX, posY);
                    break;
                default:
                    break;
            }

        }
    }

    public void setIsPromoting(boolean isPromoting){
        this.isPromoting = isPromoting;
    }

    public boolean getIsPromoting(){
        return this.isPromoting;
    }

    public void setPromotingPawn(Piece pawn){
        this.promotingPawn = pawn;
    }

    public Piece getPromotingPawn(){
        return this.promotingPawn;
    }

    public boolean getTurn(){
        return this.turn;
    }

    public void switchTurn(){
        this.turn = !this.turn;
    }

}
