package board;

import AI.StockFish;
import engine.ChessEngine;
import engine.GameTimer;
import engine.Move;
import engine.OutOfPieceMatrixException;
import network.NetworkGameState;
import network.NetworkManager;
import pieces.Piece;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Map;

import network.NetworkMove;
import pieces.Rook;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class BoardPanel extends JPanel {
    ChessEngine chessEngine = new ChessEngine();
    BoardParameters boardParam;
    private static final double MOVE_INDICATOR_SIZE_RATIO = 13.0/36.0;
    private static final double CAPTURE_INDICATOR_SIZE_RATIO = 0.935;
    private static final double SIDE_PANEL_RATIO =  1.5/8.0;
    private GameTimer gameTimer;
    private GameOverPanel gameOverPanel;
    private MenuPopUp menuPopUp;
    private NetworkManager networkManager;
    private NetworkGameState status;
    private boolean playingWithAI = false;
    private boolean isMyTurn;
    SidePanel sidePanel;
    int difficultyElo = -1;
    private boolean opponentRequestedRematch = false;
    private boolean waitingForRematch = false;
    private boolean isFirstMove = true;
    StockFish stockfish = new StockFish();
    JPanel promotionContainer;

    public BoardPanel(JFrame parentFrame) {
        setBoardAtributes(parentFrame);
        this.isMyTurn = true;
    }

    public BoardPanel(JFrame parentFrame, boolean isWhite, int difficultyElo) {
        setBoardAtributes(parentFrame);
        this.isMyTurn = true;

        boardParam.isReversed = !isWhite;
        this.difficultyElo = difficultyElo;

        playingWithAI = true;

        if(stockfish.startEngine(difficultyElo)){
            System.out.println("Engine started!");
        }else{
            System.out.println("Engine failed!");
        }

        if(chessEngine.getTurn() == boardParam.isReversed){
            startAITurn();
        }
    }

    public BoardPanel(JFrame parentFrame, NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.isMyTurn = networkManager.isHost();
        this.networkManager.setMoveReceivedCallback(this::handleReceivedMove);
        this.networkManager.setStatusReceivedCallback(this::handleNetworkStatus);
        this.networkManager.setConnectionLostCallback(this::handleConnectionLost);
        setBoardAtributes(parentFrame);
        if (!networkManager.isHost()) {
            boardParam.switchBoardOrientation();
        }
    }

    public void setBoardAtributes(JFrame parentFrame){
        chessEngine.playStartSound();
        this.setLayout(null);
        boardParam = new BoardParameters();
        boardParam.setBoardColors(
                new Color(223, 222, 222),
                new Color(181, 136, 99),
                new Color(240, 217, 181)
        );

        chessEngine.setBoardParams(boardParam);
        chessEngine.instantiatePieceArray();
        gameTimer = new GameTimer(chessEngine, this);

        this.sidePanel = new SidePanel(parentFrame);
        this.sidePanel.setChessEngine(chessEngine);
        this.sidePanel.setGameTimer(gameTimer);
        this.sidePanel.setNetworkManager(this.networkManager);

        add(sidePanel);

        gameOverPanel = new GameOverPanel(
                () -> {
                    if (networkManager != null) {
                        // MULTIPLAYER
                        if (opponentRequestedRematch) {
                            System.out.println("Accept rematch-ul!");
                            networkManager.sendGameStatus(NetworkGameState.StatusType.REMATCH_ACCEPT);
                            resetGame();
                            gameTimer.resetTimer();
                            opponentRequestedRematch = false;
                        } else {
                            System.out.println("Trimit cerere de rematch...");

                            networkManager.sendGameStatus(NetworkGameState.StatusType.REMATCH_REQUEST);

                            gameOverPanel.setTryAgainButtonText("Waiting... (1/2)");
                            gameOverPanel.setTryAgainButtonEnabled(false);

                        }
                    } else {
                        // SINGLEPLAYER
                        gameTimer.resetTimer();
                        Menu menu = new Menu(parentFrame);

                        parentFrame.getContentPane().removeAll();
                        parentFrame.add(menu);

                        if (playingWithAI)
                            menu.robotGame();
                        else {
                            menu.startGame();
                        }
                    }
                },
                () -> {
                if (networkManager != null) {
                    networkManager.sendGameStatus(NetworkGameState.StatusType.REMATCH_DECLINE);
                }
                Menu menu = new Menu(parentFrame);

                // Inlatura tabla de joc curenta si adauga meniul
                parentFrame.getContentPane().removeAll();
                parentFrame.add(menu);

                // Revalideaza layout-ul
                parentFrame.revalidate();
                parentFrame.repaint();
            }
        );

        menuPopUp = new MenuPopUp(
                () -> {
                    gameTimer.togglePause();

                    showMenuPopUp(false);

                    this.requestFocusInWindow();
                    },
                () -> {
                    if (networkManager != null) {
                        networkManager.sendGameStatus(NetworkGameState.StatusType.OPPONENT_LEFT);
                    }
                    Menu menu = new Menu(parentFrame);

                    // Inlatura tabla de joc curenta si adauga meniul
                    parentFrame.getContentPane().removeAll();
                    parentFrame.add(menu);

                    // Revalideaza layout-ul
                    parentFrame.revalidate();
                    parentFrame.repaint();
                }
        );

        add(gameOverPanel);
        add(menuPopUp);

        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e){
                handleMouseClick(e);
            }
        });
        addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                handleKeyPress(e);
            }
        });
    }

    private void handleKeyPress(KeyEvent e){
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE && !gameOverPanel.isVisible()){
            gameTimer.togglePause();
            showMenuPopUp(!menuPopUp.isVisible());
        }
    }

    private void handleMouseClick(MouseEvent e){
        if(playingWithAI && boardParam.isReversed == chessEngine.getTurn()){
            return;
        }

        if(gameOverPanel.isVisible()){
            return;
        }

        int cellSize = boardParam.cellSize;
        int margin = boardParam.margin;
        int startX = boardParam.startX;
        int startY = boardParam.startY;

        try{
            if(e.getX() < startX + margin/2 || e.getX() > startX + boardParam.boardSize - margin/2 ||
                    e.getY() < startY + margin/2 || e.getY() > startY + boardParam.boardSize - margin/2) {
                throw new OutOfBoardException("Clicked outside the board!");
            }
            int x = (e.getX() - startX - margin/2)/cellSize;
            int y = (e.getY() - margin/2)/cellSize;

            System.out.printf("x: %d, y: %d%n", x, y);

            if(x < 0 || x > 7 || y < 0 || y > 7){
                throw new OutOfBoardException("Clicked outside the board!");
            }

            int realY = boardParam.isReversed ? 7-y : y;

            Piece piece = chessEngine.piecesArray[realY][x];

            if (!isMyTurn) {
                System.out.println("Asteapta mutarea adversarului.");
                return; // Iesi din functie
            }

            if(chessEngine.doesMoveExist(x,realY)){
                System.out.printf("Moved %s to %s.%n", chessEngine.getMove(x,realY).moveAuthor().getType(), ChessEngine.getChessCoords(x,realY));

                Move currentMove = chessEngine.getMove(x,realY);
                piece = currentMove.moveAuthor();
                if(piece.getType().equals("pawn") && chessEngine.canPromote(piece)) {
                    chessEngine.setIsPromoting(true);
                    chessEngine.setPromotingPawn(piece);
                    chessEngine.setPromotionMove(currentMove);

                    printPromotionPanel(boardParam, chessEngine.getIsPromoting(), chessEngine.getPromotingPawn());
                }else{
                    if (isFirstMove) {
                        if (gameTimer != null) gameTimer.startTimer();
                        isFirstMove = false;
                    }
                    if(networkManager != null){
                        networkManager.sendMove(currentMove);
                        networkManager.setStatusReceivedCallback(this::handleNetworkStatus);
                        this.isMyTurn = false;
                    }
                    chessEngine.swapSquares(currentMove);
                    chessEngine.switchTurn();

                    if(playingWithAI && chessEngine.getTurn() == boardParam.isReversed && chessEngine.getGameState() == 0){
                        startAITurn();
                    }
                }

                chessEngine.setMovesArray(null);
                repaint();


                if(chessEngine.getGameState() != 0){
                    stockfish.stopEngine();
                    gameTimer.stopTimer();
                    showGameOverScreen(chessEngine.getGameState());
                }

            }else if(chessEngine.piecesArray[realY][x] == null){
                throw new OutOfPieceMatrixException("Selected square does not contain a chess piece!");
            }else if(chessEngine.getTurn() == piece.isWhite()){
                chessEngine.setMovesArray(piece);
                if(chessEngine.getIsPromoting()){
                    chessEngine.setIsPromoting(false);
                    chessEngine.setPromotingPawn(null);
                    chessEngine.setPromotionMove(null);
                    remove(this.promotionContainer);
                }
                System.out.println(chessEngine.getMovesArray().size());
                repaint();
            }
        } catch (OutOfBoardException | OutOfPieceMatrixException ex) {
            chessEngine.setMovesArray(null);
            if(chessEngine.getIsPromoting()){
                chessEngine.setIsPromoting(false);
                chessEngine.setPromotingPawn(null);
                chessEngine.setPromotionMove(null);
                remove(this.promotionContainer);
            }
            repaint();
            System.err.println(ex.getMessage());
        }
    }

    private void startAITurn() {

        new Thread(() -> {
        // Optional delay for realism
        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }

        Move finalMove = null;

        // --- LOGIC FOR LOW ELO ---
        // 1320 is Stockfish's approximate floor.
        // If the requested Elo is lower, we must play randomly sometimes.
        if (difficultyElo < 1320) {
            System.out.printf("is getting executed for some reason. difficulty: %d%n", difficultyElo);

            // Calculate error chance (0.0 to 1.0)
            // 250 Elo -> ~90% chance to play randomly
            // 1320 Elo -> 0% chance to play randomly
            double blunderChance = 1.0 - (difficultyElo / 1350.0);

            if (Math.random() < blunderChance) {
                System.out.println("AI is blundering intentionally (Elo " + difficultyElo + ")");
                // Pick a random legal move from our engine
                finalMove = chessEngine.getRandomMove(chessEngine.getTurn());
            }
        }

        // --- STANDARD STOCKFISH LOGIC ---
        // If we haven't picked a random move yet, ask Stockfish
        if (finalMove == null) {
            // Configure Stockfish to be as weak as possible if Elo is low
            int engineElo = Math.max(difficultyElo, 1320);

            // Limit depth severely for low Elo (makes it play "shallow" moves)
            int depth = (engineElo < 1500) ? 1 : 20;

            String bestMoveUCI = stockfish.getBestMove(chessEngine.getFenForAI(), depth);
            finalMove = chessEngine.getMoveFromUCI(bestMoveUCI);
        }

        if (finalMove != null) {
                // Handle AI Promotion (using your logic)

                if(!chessEngine.getIsPromoting()){
                    chessEngine.swapSquares(finalMove);
                }

                if(chessEngine.getIsPromoting()){
                    System.out.printf("AI move is promoting! indexAI:%d%n", chessEngine.getPromotionIndexAI());

                    chessEngine.switchPiece(finalMove.moveAuthor(), chessEngine.getPromotionIndexAI());
                    chessEngine.swapSquares(new Move(finalMove.piecePosition().x, finalMove.piecePosition().y, chessEngine.piecesArray[finalMove.moveAuthor().getPostion().y][finalMove.moveAuthor().getPostion().x]));
                    chessEngine.setIsPromoting(false);
                    chessEngine.setPromotingPawn(null);
                    chessEngine.setPromotionMove(null);
                }

                chessEngine.switchTurn();

                // 4. Update the UI on the correct thread
                SwingUtilities.invokeLater(() -> {
                    repaint();

                    // Check for game over after AI move
                    if (chessEngine.getGameState() != 0) {
                        stockfish.stopEngine();
                        gameTimer.stopTimer();
                        showGameOverScreen(chessEngine.getGameState());
                    }
                });
            }

        }).start();
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        printGame(g);
    }

    public void showGameOverScreen(int gameState){ String message = "";
        switch(gameState){
            case 1 -> message = "Checkmate! White Wins!";
            case 2 -> message = "Checkmate! Black Wins!";
            case 3 -> message = "Stalemate! Draw.";
            case 4 -> message = "Time ran out! White Wins!";
            case 5 -> message = "Time ran out! Black Wins!";
            case 6 -> message = "Draw, insufficient material";
            case 7 -> message = "Draw.";
            case 8 -> message = "Draw by repetition!";
            case 9 -> message = "White resigned. Black Wins!";
            case 10 -> message = "Black resigned. White Wins!";
            case 11 -> message = "Draw by agreement";
            case 12 -> message = "Opponent Left. You Won!";
        }

        gameOverPanel.setLabelMessage(message);
        gameOverPanel.setVisible(true);

        gameOverPanel.setBounds(0,0, getWidth(), getHeight());

        setComponentZOrder(gameOverPanel,0);
        repaint();
    }

    public void showMenuPopUp(boolean visible){
        menuPopUp.setVisible(visible);
        menuPopUp.setBounds(0,0, getWidth(), getHeight());
        setComponentZOrder(menuPopUp,0);
        repaint();
    }

    public void printGame(Graphics g){
        int boardSize = min(getWidth(), getHeight());
        int sidePanelWidth = (int) (boardSize *  SIDE_PANEL_RATIO);
        int startX = (getWidth()-boardSize-sidePanelWidth)/2;
        int startY = (getHeight() - boardSize) / 2;
        int margin = boardSize*7/100;
        int cellSize = (boardSize-margin)/8;

        this.boardParam.setBoardSizes(
                startX,
                startY,
                boardSize,
                cellSize,
                margin
        );

        chessEngine.setBoardParams(boardParam);

        printBoard(g, boardParam);

        printCoordinates(g,
                boardParam,
                "SansSerif",
                new Color(227, 227, 227)
        );

        printPieces(g, boardParam, chessEngine.piecesArray);
//        printTimer(g, boardParam);

        sidePanel.setBounds(startX + boardSize,0, sidePanelWidth, getHeight());

        printMoves(chessEngine.getMovesArray(), g, boardParam);

        gameOverPanel.setBounds(0,0, getWidth(), getHeight());
        menuPopUp.setBounds(0,0, getWidth(), getHeight());
    }

    public void printBoard(Graphics g, BoardParameters boardParam){
        //Background-ul tablei + margine
        setBackground(boardParam.backgroundColor);
        g.fillRect(boardParam.startX,boardParam.startY,boardParam.boardSize,boardParam.boardSize);

        //Patratelele care au un offset de la margine
        for(int row=0;row<8;row++){
            for(int col=0;col<8;col++){
                if(boardParam.isReversed){
                    if((row+col)%2==0){
                        g.setColor(boardParam.darkSquare);
                    }else {
                        g.setColor(boardParam.lightSquare);
                    }
                }else {
                    if((row+col)%2==1){
                        g.setColor(boardParam.darkSquare);
                    }else {
                        g.setColor(boardParam.lightSquare);
                    }
                }

                int xPos = getXPos(boardParam, col);
                int yPos = getYPos(boardParam, row);

                int size =  boardParam.cellSize;

                g.fillRect(xPos,yPos,size,size);
            }
        }
    }

    public int getXPos(BoardParameters boardParam, int col){
        return boardParam.startX + col*boardParam.cellSize + boardParam.margin/2;
    }

    public int getYPos(BoardParameters boardParam, int row){
        return boardParam.startY + row*boardParam.cellSize + boardParam.margin/2;
    }

    public void printCoordinates(Graphics g, BoardParameters boardParam, String fontFamily, Color color){
        //setari font
        /*
          marimea fontului este un procent din marimea unui patrat de joc
        */
        int fontSize = max(10, boardParam.cellSize/5);
        Font font = new Font(fontFamily, Font.BOLD, fontSize);
        g.setFont(font);
        g.setColor(color);

        FontMetrics fm = g.getFontMetrics();

        String columns;
        String rows;

        if(boardParam.isReversed){
            columns = "hgfedcba";
            rows = "12345678";
        }else{
            columns = "abcdefgh";
            rows = "87654321";
        }

        for(int cell=0;cell<8;cell++){
            /*
              Calculam pozitia coordonatelor
              */
            int[] coordinatesColumns = getCoordinatesPos(boardParam, cell, fm, true);
            int[] coordinatesRows = getCoordinatesPos(boardParam, cell, fm, false);

            //columns
            g.drawString(columns.substring(cell, cell+1),coordinatesColumns[0],coordinatesColumns[1]);
            g.drawString(columns.substring(cell, cell+1),coordinatesColumns[0],coordinatesColumns[1] + boardParam.boardSize - boardParam.margin/2);
            //rows
            g.drawString(rows.substring(cell, cell+1),coordinatesRows[0],coordinatesRows[1]);
            g.drawString(rows.substring(cell, cell+1),coordinatesRows[0] + boardParam.boardSize - boardParam.margin/2,coordinatesRows[1]);
        }
    }

    public static int[] getCoordinatesPos(BoardParameters boardParam, int index, FontMetrics fontMetrics, boolean isColumn){
        int[] coordinates = new int[2];

        int width = fontMetrics.stringWidth("a");
        int height = fontMetrics.getAscent();

        int x = boardParam.startX;
        int y = boardParam.startY;
        int size = boardParam.cellSize;
        int margin = boardParam.margin;

        if(isColumn){
            coordinates[0]=x+(size*(2*index+1)+margin-width)/2;
            coordinates[1]=y+(margin+height)/4;
        }else{
            coordinates[0]=x+(margin-2*width)/4;
            coordinates[1]=y+(size*(2*index+1)+margin+height)/2;
        }

        return coordinates;
    }

    public void printPieces(Graphics g, BoardParameters boardParam, Piece[][] pieces){
        int pieceCount =0;

        if(boardParam.isReversed){
            for(int row = 7;row >=0;row--){
                for(int col = 7;col >=0;col--){
                    pieceCount = drawChessPiece(g, boardParam, pieces, pieceCount, row, col);

                }
            }
        }else{
            for(int row = 0;row < 8;row++){
                for(int col = 0;col < 8;col++){
                    pieceCount = drawChessPiece(g, boardParam, pieces, pieceCount, row, col);

                }
            }
        }
    }

    private int drawChessPiece(Graphics g, BoardParameters boardParam, Piece[][] pieces, int pieceCount, int row, int col) {
        if(pieces[row][col] != null){
            pieceCount++;
            boolean isReversed = boardParam.isReversed;
            int xPos = getXPos(boardParam, col);
            int yPos = isReversed ? getYPos(boardParam, 7-row) :getYPos(boardParam, row);

            g.drawImage(pieces[row][col].getImage(), xPos, yPos, boardParam.cellSize, boardParam.cellSize, this);
        }
        return pieceCount;
    }

    public void printMoves(Map<String, Move> moves, Graphics g, BoardParameters boardParam){
        if(moves == null){
            return;
        }

//        System.out.println("Drawing moves ...");

        g.setColor(new Color(1,0,0, 82));

        moves.forEach((key,move)->{
            int x = move.piecePosition().x;
            int y = boardParam.isReversed ? 7-move.piecePosition().y : move.piecePosition().y;


            int xPos = getXPos(boardParam, x);
            int yPos = getYPos(boardParam, y);

            int moveSize = (int)(boardParam.cellSize*MOVE_INDICATOR_SIZE_RATIO);
            int captureSize = (int)(boardParam.cellSize*CAPTURE_INDICATOR_SIZE_RATIO);


//            g.fillRect(xPos,yPos,boardParam.cellSize,boardParam.cellSize);
            if(move.isCapture()){
                Graphics2D g2 = (Graphics2D) g;

                g2.setStroke(new BasicStroke(boardParam.cellSize - captureSize));

                xPos = xPos + (boardParam.cellSize - captureSize) /2;
                yPos = yPos + (boardParam.cellSize - captureSize) /2;

                g2.drawArc(xPos, yPos, captureSize, captureSize, 0, 360);

            }else{
                xPos = xPos + (boardParam.cellSize - moveSize) /2;
                yPos = yPos + (boardParam.cellSize - moveSize) /2;

                g.fillArc(xPos,yPos,moveSize, moveSize, 0, 360);
            }
        });
    }

    public void printPromotionPanel(BoardParameters boardParam, boolean isVisible, Piece pawn) {
        if (!isVisible || pawn == null) {
            return;
        }

        JPanel promotionContainer = new JPanel();
        promotionContainer.setLayout(null); // Folosim null layout pentru poziționare absolută (cum ai tu deja calculat)
        promotionContainer.setBounds(0, 0, getWidth(), getHeight()); // Să acopere tot BoardPanel-ul
        promotionContainer.setOpaque(false); // Transparent, ca să se vadă tabla sub el

        int cellSize = boardParam.cellSize;
        String[] pieces = {"queen", "rook", "bishop", "knight"};
        ImageIcon[] images = new ImageIcon[pieces.length];

        for (int i = 0; i < images.length; i++) {
            URL imgUrl = getClass().getResource("/data/pieces/" + (pawn.isWhite() ? "white" : "black") + "/" + pieces[i] + ".png");
            if (imgUrl != null) {
                images[i] = new ImageIcon(imgUrl);
            }
        }

        int posX = pawn.getPostion().x;
        int posY = pawn.getPostion().y;
        int boardPosY = boardParam.isReversed ? 7 - pawn.getPostion().y : pawn.getPostion().y;
        int startX = boardParam.startX;
        int startY = boardParam.startY;
        int margin = boardParam.margin;

        for (int i = 0; i <= 3; i++) {
            JButton button = new JButton(new ImageIcon(images[i].getImage().getScaledInstance(cellSize / 2, cellSize / 2, Image.SCALE_SMOOTH)));
            button.setSize(cellSize / 2, cellSize / 2);

            button.setLocation((startX + posX * cellSize + margin / 2) - cellSize / 2, (startY + (boardPosY - 1) * cellSize + margin / 2) + cellSize / 2 * i);

            int finalI = i;
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Move promotionMove = chessEngine.getPromotionMove();
                    if (promotionMove != null) {
                        chessEngine.switchPiece(chessEngine.piecesArray[posY][posX], finalI);
                        chessEngine.swapSquares(new Move(promotionMove.piecePosition().x, promotionMove.piecePosition().y, chessEngine.piecesArray[posY][posX]));
                    } else {
                        System.err.println("Warning: promotionMove is null");
                    }

                    chessEngine.setIsPromoting(false);
                    chessEngine.setPromotingPawn(null);
                    chessEngine.setPromotionMove(null);
                    chessEngine.switchTurn();

                     if(playingWithAI && chessEngine.getTurn() == boardParam.isReversed && chessEngine.getGameState() == 0){
                        startAITurn();
                     }

                    remove(promotionContainer);

                    revalidate();
                    repaint();
                }
            });

            promotionContainer.add(button);
        }

        this.promotionContainer = promotionContainer;
        add(promotionContainer);

        setComponentZOrder(promotionContainer, 0);

        promotionContainer.requestFocusInWindow();

        repaint();
    }

    private String formatTime(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }


    private void handleReceivedMove(NetworkMove netMove) {
        // 1. Reconstruieste obiectul Move din NetworkMove
        Piece movedPiece = chessEngine.piecesArray[netMove.fromY][netMove.fromX];

        if (movedPiece == null) {
            System.err.println("Eroare: Mutare primita pentru o pozitie goala.");
            return;
        }

        // Daca nu e o mutare speciala (rocada, enpassant) se poate folosi Move simplu
        // Pentru rocarie, trebuie gasita tura in pozitia initiala
        Rook rook = null;
        if (netMove.isCastle) {
            int rookX = netMove.toX > netMove.fromX ? 7 : 0;
            rook = (Rook) chessEngine.piecesArray[netMove.fromY][rookX];
        }

        Move currentMove = new Move(netMove.toX, netMove.toY, movedPiece, netMove.isCapture, netMove.isEnpassant, netMove.isCastle, rook);

        if (isFirstMove) {
            if (gameTimer != null) {
                System.out.println("Start Timer (Received Move)");
                gameTimer.startTimer();
            }
            isFirstMove = false;
        }
        // 2. Executa mutarea pe tabla locala
        chessEngine.swapSquares(currentMove);

        // 3. Actualizeaza starea
        chessEngine.switchTurn();
        this.isMyTurn = true; // Acum este rândul jucatorului local

        // 4. Inverseaza orientarea tablei
//        boardParam.switchBoardOrientation();

        // 5. Redeseneaza tabla
        repaint();
    }
    private void handleNetworkStatus(NetworkGameState status) {
        SwingUtilities.invokeLater(() -> {
            switch (status.statusType) {

                case RESIGN -> {
                    System.out.println("Adversarul a cedat!");
                    if (gameTimer != null) gameTimer.stopTimer();
                    boolean opponentIsWhite = !networkManager.isHost();
                    chessEngine.forceResign(opponentIsWhite);
                    showGameOverScreen(chessEngine.getGameState());
                    gameTimer.resetTimer();
                }
                case DRAW_OFFER -> {
                    System.out.println("Am primit oferta de remiza.");
                    if (sidePanel != null) {
                        sidePanel.showDrawOffer(); // Metodă care face vizibil butonul de accept
                    }
                }

                case DRAW_DECLINE -> {
                    JOptionPane.showMessageDialog(this, "Opponent declined the draw.");
                    if (sidePanel != null) sidePanel.enableDrawButton();
                    System.out.println("Draw offer declined.");
                }

                case DRAW_ACCEPT -> {
                    System.out.println("Remiza acceptata!");
                    if (gameTimer != null) gameTimer.stopTimer();
                    showGameOverScreen(11);
                }

                //Adversarul cere Rematch
                case REMATCH_REQUEST -> {
                    opponentRequestedRematch = true;
                    if (waitingForRematch) {
                        System.out.println("Race condition: Ambii au cerut simultan. Resetam!");
                        resetGame();
                        gameTimer.resetTimer();
                    } else if(gameOverPanel != null && gameOverPanel.isVisible()) {
                            gameOverPanel.setTryAgainButtonText("Try Again (1/2) - Opponent Ready");
                            gameOverPanel.repaint();
                        }
                }
                case REMATCH_DECLINE-> {
                    if (gameOverPanel != null && gameOverPanel.isVisible()) {
                        gameOverPanel.setTryAgainButtonText("Rematch Declined");
                        gameOverPanel.setTryAgainButtonEnabled(false);
                    }
                }

                //Rematch Acceptat
                case REMATCH_ACCEPT -> {
                    System.out.println("Am ajuns aici bos");
                    resetGame();
                    gameTimer.resetTimer();
                    repaint();
                }
                case OPPONENT_LEFT -> {
                    System.out.println("Opponent Left");
                    showGameOverScreen(12);
                    gameOverPanel.setTryAgainButtonText("Opponent Left");
                    gameOverPanel.setTryAgainButtonEnabled(false);
                    gameOverPanel.setTryAgainButtonVisible(false);
                }

            }
        });
    }

    private void handleConnectionLost() {
        // Dacă suntem în meniul de așteptare Rematch
        if (waitingForRematch && gameOverPanel.isVisible()) {
            gameOverPanel.setTryAgainButtonText("Opponent Left");
            gameOverPanel.setTryAgainButtonEnabled(false);
        }
        else if (!chessEngine.isGameOver()) {
            if (gameTimer != null){ gameTimer.stopTimer();}
            showGameOverScreen(12);
        }
    }

    public void resetGame() {
        System.out.println(">>> RESET GAME (UI + ENGINE) <<<");

        chessEngine.resetGame();

        //resetare parametrii vizuali
        chessEngine.setBoardParams(boardParam);

        //variabilele de Multiplayer
        opponentRequestedRematch = false;
        waitingForRematch = false;
        isFirstMove = true;

        if (networkManager != null) {
            this.isMyTurn = networkManager.isHost();
            boardParam.isReversed = !networkManager.isHost();
        } else {
            // Singleplayer
            this.isMyTurn = true;
        }
        if (gameTimer != null) {
            gameTimer.resetTimer();
        }
        gameTimer = new GameTimer(chessEngine, this);

        if (sidePanel != null) {
            sidePanel.setGameTimer(gameTimer);
            sidePanel.resetSidePanel();
        }

        if (gameOverPanel != null) {
            gameOverPanel.setVisible(false);
            gameOverPanel.setTryAgainButtonText("Try Again");
            gameOverPanel.setTryAgainButtonEnabled(true);
        }
        repaint();
        chessEngine.playStartSound();
        this.requestFocusInWindow();//escape panel

    }
}

/*
70: a8, 71: b8, 72: c8, 73: d8, 74: e8, 75: f8, 76: g8, 77: h8
.
.
ij: column[j]row[i]
.
.
00: a1, 01: b1, 02: c1, 03: d1, 04: e1, 05: f1, 06: g1, 07: h1
 */

//ex fen: "r1bk3r/p2pBpNp/n4n2/1p1NP2P/6P1/3P4/P1P1K3/q5b1"

