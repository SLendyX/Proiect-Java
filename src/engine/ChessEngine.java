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
//    "k7/8/8/8/8/8/8/3NK3"
//    "KQkq"
    BoardParameters boardParam;
    String defaultFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
    String castleFen = "KQkq";
    Map<String, Integer> positionMap = new HashMap<>();

    boolean isPromoting = false;
    boolean turn = true;
    Piece promotingPawn;
    Move promotionMove;
    Pawn enPassantPawn;
    King castledKing;
    boolean isChecked;
    int gameState;
    private int halfMoveClock = 0;
    private boolean whiteResigned = false;
    private boolean blackResigned = false;
    private boolean drawAgreed = false;
    private int promotionIndexAI = -1;

    private final Clip moveSound;
    private final Clip captureSound;
    private final Clip castleSound;
    private final Clip checkSound;
    private final Clip gameStartSound;
    private final Clip gameEndSound;
    private final Clip promotionSound;

    public Piece[][] piecesArray;
    private Map<String, Move> movesArray;

    public ChessEngine(){
        this.piecesArray = null;
        this.movesArray = null;
        this.moveSound = loadClip("/data/audio/move-self.wav");
        this.captureSound = loadClip("/data/audio/capture.wav");
        this.castleSound  = loadClip("/data/audio/castle.wav");
        this.checkSound = loadClip("/data/audio/move-check.wav");
        this.gameStartSound = loadClip("/data/audio/game-start.wav");
        this.gameEndSound = loadClip("/data/audio/game-end.wav");
        this.promotionSound = loadClip("/data/audio/promote.wav");
    }

    public String getDefaultFen() {
        return defaultFen;
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

    public void playStartSound() {
        playSound(gameStartSound);
    }

    public void playEndSound(){
        playSound(gameEndSound);
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

    public void modifyCastleFen(boolean isWhite){
        if(castleFen.length() > 2){
            castleFen = isWhite ? "kq" : "KQ";
        }else{
            castleFen = "-";
        }
    }

    public void modifyCastleFenForRook(Rook rook){
        int x = rook.getPostion().x;
        if(x!=0 && x!=7)
            return;

        boolean isWhite = rook.isWhite();

        char symbol = x == 0 ? 'q' : 'k';
        symbol = isWhite ? Character.toUpperCase(symbol) : symbol;

        StringBuilder builder = new StringBuilder();

        for(int i=0; i <castleFen.length(); i++){
            if(symbol != castleFen.charAt(i)){
                builder.append(castleFen.charAt(i));
            }
        }

        castleFen = builder.toString();

        if(castleFen.isEmpty()){
            castleFen = "-";
        }
    }


    public void swapSquares(Move move){
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

        if(piece instanceof King king){
            if(!king.hasMoved()){
                modifyCastleFen(king.isWhite());
            }
        }else if(piece instanceof Rook rook){
            if(!rook.hasMoved()){
                modifyCastleFenForRook(rook);
            }
        }

        piecesArray[moveY][moveX].setPosition(moveX, moveY);

        if(getEnPassantPawn() != null){
            getEnPassantPawn().setCanEnPassant(false);
            setEnPassantPawn(null);
        }

        if (piece instanceof Pawn || move.isCapture()) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }

        if(piecesArray[moveY][moveX] instanceof Pawn pawn){
            pawn.setCanEnPassant(Math.abs(moveY - pieceY) == 2);
            if(pawn.getCanEnpassant())
                setEnPassantPawn(pawn);
        }

        if(!piecesArray[moveY][moveX].hasMoved()){
            piecesArray[moveY][moveX].setHasMoved(true);
        }


        updatePositions();

        setIsChecked(!getTurn());
        setGameState(!getTurn());

        if(getGameState() != 0){
            playSound(gameEndSound);
        }else if(isChecked()){
            playSound(checkSound);
        }else if(getIsPromoting()){
            playSound(promotionSound);
        }else if(move.isCapture()){
            playSound(captureSound);
        }else if(move.isCastle()) {
            playSound(castleSound);
        }else{
            playSound(moveSound);
        }

        System.out.printf("Full FEN:%s (count: %d)%n", getFullFen(), positionMap.get(getFullFen()));
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
                //checks if text is a number
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

    public String getFullFen(){
        StringBuilder fen = new StringBuilder();
        fen.append(getCurrentFen()).append(" ");

        fen.append(castleFen).append(" ");

        if(enPassantPawn == null){
            fen.append("-");
        }else{
            fen.append(getChessCoords(enPassantPawn.getPostion(), boardParam.isReversed));
        }

        return fen.toString();
    }

    public String getFenForAI(){
        StringBuilder fen = new StringBuilder();
        fen.append(getCurrentFen()).append(" ");
        fen.append(getTurn() ? "w" : "b").append(" ");
        fen.append(castleFen).append(" ");

        if(enPassantPawn == null){
            fen.append("-");
        }else{
            fen.append(getChessCoords(enPassantPawn.getPostion(), boardParam.isReversed));
        }
        fen.append(" ");

        fen.append(halfMoveClock).append(" ");
        fen.append(halfMoveClock/2);

        return fen.toString();
    }



    public void updatePositions(){
        String key = getFullFen();
        int counter = positionMap.get(key) == null ? 1 : positionMap.get(key) + 1;

        positionMap.put(key, counter);
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

    public static String getChessCoords(PiecePosition position, boolean isReversed){
        int x = position.x;
        int y = position.y;

        return getChessCoords(x, y, isReversed);
    }

    public Move getMoveFromUCI(String uciMove) {
        // 1. Parse Start Coordinate (e.g., "e2")
        int fromX = uciMove.charAt(0) - 'a'; // 'e' -> 4
        // Convert rank '2' to index 6 (because your row 0 is rank 8)
        int fromY = 8 - (uciMove.charAt(1) - '0');

        // 2. Parse Destination Coordinate (e.g., "e4")
        int toX = uciMove.charAt(2) - 'a';
        int toY = 8 - (uciMove.charAt(3) - '0');

        // 3. Retrieve the Piece from your board array
        Piece movingPiece = piecesArray[fromY][fromX];

        // Safety check (shouldn't happen if engine is synced)
        if (movingPiece == null) {
            System.err.println("Error: Stockfish tried to move from " + uciMove + " but square is empty.");
            return null;
        }

        // 4. Detect Special Move Attributes for your Move Record
        Piece targetPiece = piecesArray[toY][toX];
        boolean isCapture = (targetPiece != null);

        // Check Castling (King moves 2 squares)
        boolean isCastle = (movingPiece instanceof King) && Math.abs(fromX - toX) == 2;

        if(isCastle){
            setCastledKing((King) movingPiece);
        }

        // Check En Passant (Pawn moves diagonally to empty square)
        boolean isEnPassant = (movingPiece instanceof Pawn)
                && (fromX != toX)
                && (targetPiece == null);

        // 5. Handle Promotion (UCI string length is 5, e.g., "a7a8q")
        if (uciMove.length() == 5) {
            String promotions = "qrbn";

            setPromotionIndexAI(promotions.indexOf(uciMove.charAt(4)));

            this.setIsPromoting(true);
            this.setPromotingPawn(movingPiece);
        }

        Rook castleRook = isCastle ? (Rook) (fromX - toX < 0 ? piecesArray[fromY][7] : piecesArray[fromY][0]) : null;

        // 6. Return your custom Move object
        return new Move(toX, toY, movingPiece, isCapture, isEnPassant, isCastle, castleRook);
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


    private boolean hasInsufficientMaterial(){
        List<Piece> allPieces = new ArrayList<>();
        boolean whiteHasKnight = false;
        boolean blackHasKnight = false;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = piecesArray[r][c];
                if(p != null) {
                    if(p instanceof Queen ||p instanceof Rook || p instanceof Pawn){
                        return false;
                    }

                    allPieces.add(p);
                    if(p instanceof Knight){
                        if(p.isWhite()) whiteHasKnight = true;
                        else blackHasKnight = true;
                    }
                }
            }
        }

        if(allPieces.size() == 2){
            return true;
        }
        if(allPieces.size() == 3){
            return true;
        }
        if(!whiteHasKnight && !blackHasKnight) {
            int firstSquareColor = -1;

            for (Piece p : allPieces) {
                if (p instanceof Bishop) {
                    int x = p.getPostion().x;
                    int y = p.getPostion().y;
                    int currentSquareColor = (x + y) % 2;

                    if (firstSquareColor == -1) {
                        firstSquareColor = currentSquareColor;
                    } else if (firstSquareColor != currentSquareColor) {
                        return false;
                    }
                }
            }
            return true;
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

            PiecePosition kingPosition = getKingPos(cloned, piece.isWhite());

            // if king square is attacked, illegal move
            if (isSquareAttacked(kingPosition.x, kingPosition.y, piece.isWhite(), cloned)) {
                continue;
            }

            legal.add(m);
        }

        return legal.toArray(new Move[0]);
    }

    public Move getRandomMove(boolean isWhite) {
        List<Move> allLegalMoves = new ArrayList<>();

        // 1. Loop through all pieces
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = piecesArray[row][col];

                // 2. If it's the AI's piece, get its legal moves
                if (p != null && p.isWhite() == isWhite) {
                    Move[] moves = filterLegalMoves(p, piecesArray);
                    allLegalMoves.addAll(Arrays.asList(moves));
                }
            }
        }

        // 3. Return a random one
        if (!allLegalMoves.isEmpty()) {
            Move randomMove = allLegalMoves.get(new Random().nextInt(allLegalMoves.size()));

            if(randomMove.moveAuthor() instanceof Pawn && (randomMove.piecePosition().y == 0 || randomMove.piecePosition().y == 7)){
                Random random = new Random();

                setPromotionIndexAI(random.nextInt(4));

                this.setIsPromoting(true);
                this.setPromotingPawn(randomMove.moveAuthor());
            }

            return randomMove;
        }
        return null;
    }

    public PiecePosition getKingPos(Piece[][] board, boolean turn){
        int kingX = -1;
        int kingY = -1;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p instanceof King && p.isWhite() == turn) {
                    kingX = c;
                    kingY = r;
                }
            }
        }

        return new PiecePosition(kingX, kingY);
    }

    public void setIsChecked(boolean turn){
        PiecePosition kingPosition = getKingPos(this.piecesArray, turn);
        this.isChecked = isSquareAttacked(kingPosition.x, kingPosition.y, turn, this.piecesArray);
    }

    public void resign() {
        if (turn) {
            whiteResigned = true;
        } else {
            blackResigned = true;
        }
        System.out.println("W = " + whiteResigned + " B = " + blackResigned);
        setGameState(turn);
        playEndSound();
    }

    public void agreeDraw() {
        drawAgreed = true;
        setGameState(turn);
        System.out.println("Draw?: " + drawAgreed);
        playEndSound();
    }

    public boolean isGameOver() {
        return gameState != 0;
    }

    public int calculateGameState(boolean turn) {
        if (whiteResigned) {
            return 9;
        }
        if (blackResigned) {
            return 10;
        }
        if (drawAgreed) {
            return 11;
        }
        if(hasInsufficientMaterial()){
            return 6;
        }else if(halfMoveClock >= 100){
            return 7;
        }else if(positionMap.get(getFullFen()) != null && positionMap.get(getFullFen()) >=3){
            return 8;
        }
        boolean hasLegalMoves = false;
        outerLoop:
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = piecesArray[r][c];
                if (p != null && p.isWhite() == turn) {
                    Move[] legalMoves = filterLegalMoves(p, this.piecesArray);
                    if (legalMoves.length > 0) {
                        hasLegalMoves = true;
                        break outerLoop;
                    }
                }
            }
        }

        if (hasLegalMoves) {
            return 0;
        }

        if (isChecked()) {
            return getTurn() ? 1 : 2;
        }else{
            return 3;
        }
    }

    public void resetGame() {
        this.piecesArray = null;
        this.movesArray = null;
        this.turn = true;
        this.isPromoting = false;
        this.promotingPawn = null;
        this.promotionMove = null;
        this.enPassantPawn = null;
        this.castledKing = null;
        this.isChecked = false;
        this.halfMoveClock = 0;
        this.gameState = 0;
        this.whiteResigned = false;
        this.blackResigned = false;
        this.drawAgreed = false;
        instantiatePieceArray(defaultFen);
    }

    public boolean isChecked() {
        return isChecked;
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

    public void setGameState(boolean turn) {
        this.gameState = calculateGameState(turn);
    }

    public int getGameState() {
        return this.gameState;
    }

    public int getPromotionIndexAI() {
        return promotionIndexAI;
    }

    public void setPromotionIndexAI(int promotionIndexAI) {
        this.promotionIndexAI = promotionIndexAI;
    }
}
