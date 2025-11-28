package engine;

import board.BoardParameters;
import pieces.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;
import java.util.*;

public class ChessEngine {
    //    String defaultFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
    BoardParameters boardParam;
    String defaultFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
    boolean isPromoting = false;
    boolean turn = true;
    Piece promotingPawn;
    Move promotionMove;
    Pawn enPassantPawn;
    King castledKing;

    private final Clip moveSound;
    private final Clip captureSound;
    private final Clip castleSound;

    public Piece[][] piecesArray;
    private Map<String, Move> movesArray;

    public ChessEngine(){
        this.piecesArray = null;
        this.movesArray = null;
        this.moveSound = loadClip("/data/audio/move-self.wav");
        this.captureSound = loadClip("/data/audio/capture.wav");
        this.castleSound  = loadClip("/data/audio/castle.wav");
    }

    private Clip loadClip(String path) {

        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("Audio not found: " + path);
                return null;
            }

            AudioInputStream stream = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            return clip;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void playSound(Clip clip) {
        if (clip == null) return;

        clip.stop();       // stop if still playing
        clip.setFramePosition(0);
        clip.start();      // play again from start
    }

    public void setCastledKing(King castledKing) {
        this.castledKing = castledKing;
    }

    public King getCastledKing() {
        return castledKing;
    }

    public void setEnPassantPawn(Pawn enPassantPawn) {
        this.enPassantPawn = enPassantPawn;
    }

    public Pawn getEnPassantPawn(){
        return this.enPassantPawn;
    }

    public void setMovesArray(Piece piece){
        if (piece == null) {
            this.movesArray = null;
            return;
        }

        Map<String, Move> movesMap = new HashMap<>();

        Move[] moves = filterLegalMoves(piece, piecesArray);

        for(Move move : moves){
            String key = move.piecePosition().chessCoordinate;
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
        if(move.isCapture()){
            playSound(captureSound);
        }else if(move.isCastle()){
            playSound(castleSound);
        }else{
            playSound(moveSound);
        }

        int moveX = move.piecePosition().x;
        int moveY = move.piecePosition().y;

        Piece piece = move.moveAuthor();

        int pieceX = piece.getPostion().x;
        int pieceY = piece.getPostion().y;

        piecesArray[moveY][moveX] = piece;
        piecesArray[pieceY][pieceX] = null;

        if(move.isEnpassant() && piecesArray[pieceY][moveX].isWhite() != getTurn()){
            piecesArray[pieceY][moveX] = null;
        }else if(move.isCastle()){
            boolean direction = moveX - pieceX >= 0;
            int dirX = direction ? pieceX + 1 : pieceX - 1;

            int rookX = move.rook().getPostion().x;
            int rookY = move.rook().getPostion().y;

            System.out.println(rookX + " " + rookY);


            piecesArray[pieceY][dirX] = move.rook();
            piecesArray[pieceY][dirX].setHasMoved(true);
            piecesArray[pieceY][dirX].setPosition(dirX, pieceY);

            piecesArray[rookY][rookX] = null;
        }

        piecesArray[moveY][moveX].setPosition(moveX, moveY);

        if(getEnPassantPawn() != null){
            getEnPassantPawn().setCanEnPassant(false);
            setEnPassantPawn(null);
        }

        if(piecesArray[moveY][moveX] instanceof Pawn pawn){
            pawn.setCanEnPassant(Math.abs(moveY - pieceY) == 2);
            setEnPassantPawn(pawn);
        }

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

    public static String getChessCoords(int x, int y){
        String cols = "abcdefgh";
        String rows = "87654321";

        return String.valueOf(cols.charAt(x)) +
                rows.charAt(y);
    }

    public static String getChessCoords(int x, int y, boolean isReversed){
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
        System.out.println("Can promote pawn");
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

    public boolean isSquareAttacked(int x, int y, boolean turn, Piece[][] pieces) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                Piece p = pieces[row][col];
                if (p == null) continue;
                if (p.isWhite() == turn) continue;

                Move[] enemyMoves = p.getMoves(pieces);
                for (Move m : enemyMoves) {
                    if(getCastledKing() != null){
                        int direction = x > 4 ? -1  : 1;

                        if (m.piecePosition().x == x+direction && m.piecePosition().y == y) {
                            return true;
                        }
                    }

                    if (m.piecePosition().x == x && m.piecePosition().y == y) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Piece[][] cloneBoard(Piece[][] board) {
        Piece[][] cloned = new Piece[8][8];
        for(int r = 0; r < 8; r++){
            System.arraycopy(board[r], 0, cloned[r], 0, 8);
        }

        return cloned;
    }

    public Move[] filterLegalMoves(Piece piece, Piece[][] board) {

        List<Move> legal = new ArrayList<>();

        Move[] rawMoves = piece.getMoves(board);
        if (rawMoves == null) return new Move[0];

        for (Move m : rawMoves) {

            Piece[][] cloned = cloneBoard(board);

            int fromX = piece.getPostion().x;
            int fromY = piece.getPostion().y;
            int toX   = m.piecePosition().x;
            int toY   = m.piecePosition().y;


            cloned[fromY][fromX] = null;
            cloned[toY][toX] = piece;

            setCastledKing(null);

            if(m.isEnpassant() && cloned[fromY][toX].isWhite() != getTurn()) {
                cloned[fromY][toX] = null;
            }else if(m.isCastle()){
                if(isSquareAttacked(fromX, fromY, getTurn(), cloned)){
                    continue;
                }

                boolean direction = toX - fromX >= 0;
                int dirX = direction ? fromX + 1 : fromX - 1;

                int rookX = m.rook().getPostion().x;
                int rookY = m.rook().getPostion().y;

                cloned[fromY][dirX] = cloned[rookY][rookX];

                cloned[rookY][rookX] = null;

                if(cloned[toY][toX] instanceof King king){
                    setCastledKing(king);
                    System.out.println(getCastledKing());
                }

            }

            int kingX = -1;
            int kingY = -1;

            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    Piece p = cloned[r][c];
                    if (p instanceof King && p.isWhite() == piece.isWhite()) {
                        kingX = c;
                        kingY = r;
                    }
                }
            }

            // if king square is attacked, illegal move
            if (isSquareAttacked(kingX, kingY, piece.isWhite(), cloned)) {
                continue;
            }

            legal.add(m);
        }

        return legal.toArray(new Move[0]);
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

    public void setPromotionMove(Move move){
        this.promotionMove = move;
    }

    public Move getPromotionMove(){
        return this.promotionMove;
    }

}
