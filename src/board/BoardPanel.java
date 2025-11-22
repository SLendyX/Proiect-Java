package board;

import engine.ChessEngine;
import engine.Move;
import engine.OutOfPieceMatrixException;
import pieces.Piece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class BoardPanel extends JPanel {
    ChessEngine chessEngine = new ChessEngine();
    BoardParameters boardParam;
    private static final double MOVE_INDICATOR_SIZE_RATIO = 13.0/36.0;
    private static final double CAPTURE_INDICATOR_SIZE_RATIO = 0.935;

    public BoardPanel() {
        boardParam = new BoardParameters();
//        boardParam.switchBoardOrientation();

        boardParam.setBoardColors(
                new Color(223, 222, 222),
                new Color(181, 136, 99),
                new Color(240, 217, 181)
        );

        chessEngine.setBoardParams(boardParam);
        chessEngine.instantiatePieceArray();

        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e){
                handleMouseClick(e);
            }
        });

    }


    private void handleMouseClick(MouseEvent e){
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


            if(chessEngine.doesMoveExist(x,realY)){
                System.out.printf("Moved %s to %s.%n", chessEngine.getMove(x,realY).moveAuthor().getType(), chessEngine.getChessCoords(x,realY));

                Move currentMove = chessEngine.getMove(x,realY);
                piece = currentMove.moveAuthor();
                if(piece.getType().equals("pawn") && chessEngine.canPromote(piece)) {
                    chessEngine.setIsPromoting(true);
                    chessEngine.setPromotingPawn(piece);
                    chessEngine.setPromotionMove(currentMove);
                }else{
                    chessEngine.swapSquares(currentMove);
                    chessEngine.switchTurn();
                }
                chessEngine.setMovesArray(null);
                repaint();
            }else if(chessEngine.piecesArray[realY][x] == null){
                throw new OutOfPieceMatrixException("Selected square does not contain a chess piece!");
            }else if(chessEngine.getTurn() == piece.isWhite()){
                chessEngine.setMovesArray(piece);
                if(chessEngine.getIsPromoting()){
                    chessEngine.setIsPromoting(false);
                    chessEngine.setPromotingPawn(null);
                    chessEngine.setPromotionMove(null);
                    removeAll();
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
                removeAll();
            }
            repaint();
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        printGame(g);
    }

    public void printGame(Graphics g){

        int boardSize = min(getWidth(), getHeight());
        int startX = (getWidth()-boardSize)/2;
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
        printTimer(g, boardParam);

        printMoves(chessEngine.getMovesArray(), g, boardParam);

        printPromotionPanel(boardParam, chessEngine.getIsPromoting(), chessEngine.getPromotingPawn());
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

        System.out.println("Drawing moves ...");

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
        if(!isVisible || pawn == null){
            System.out.println("Deleting panel");
            return ;
        }

        int cellSize = boardParam.cellSize;

        String[] pieces = {"queen", "rook", "bishop", "knight"};

        ImageIcon[] images = new ImageIcon[pieces.length];

        for(int i=0;i<images.length;i++){
            images[i] = new ImageIcon("data/pieces/"+ (pawn.isWhite() ? "white" : "black") +"/"+ pieces[i] +".png");
        }

        int posX = pawn.getPostion().x;
        int posY = pawn.getPostion().y;
        int boardPosY = boardParam.isReversed ? 7 - pawn.getPostion().y : pawn.getPostion().y;
        int startX = boardParam.startX;
        int startY = boardParam.startY;
        int margin = boardParam.margin;

//        g.setColor(new Color(73,54,87));
        for (int i = 0; i <= 3; i++) {
            JButton button = new JButton(new ImageIcon(images[i].getImage().getScaledInstance(cellSize/2, cellSize/2, Image.SCALE_SMOOTH)));
            button.setSize(cellSize/2, cellSize/2);
            button.setLocation((startX + posX*cellSize + margin/2) - cellSize / 2, (startY + (boardPosY-1)*cellSize+margin/2) + cellSize / 2 * i);

            int finalI = i;
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Move promotionMove = chessEngine.getPromotionMove();
                    if(promotionMove != null) {
                        // First swap the pawn to the promotion square
                        chessEngine.swapSquares(promotionMove);
                        // Then promote the piece at its new position
                        int newX = promotionMove.piecePosition().x;
                        int newY = promotionMove.piecePosition().y;
                        chessEngine.switchPiece(chessEngine.piecesArray[newY][newX], finalI);
                    } else {
                        // This should never happen - promotionMove should always be set when isPromoting is true
                        System.err.println("Warning: promotionMove is null");
                    }
                    chessEngine.setIsPromoting(false);
                    chessEngine.setPromotingPawn(null);
                    chessEngine.setPromotionMove(null);
                    chessEngine.switchTurn();

                    removeAll();
                    repaint();
                }
            });
            add(button);
        }
    }

    public void printTimer(Graphics g, BoardParameters boardParam){
        int cellSize = boardParam.cellSize;
        int fontSize = max(10, cellSize/5);
        Font font = new Font("Arial", Font.BOLD, fontSize);

        g.setFont(font);
        g.setColor(new Color(1,1,1));

        String timpPlayer1 = "5:00";
        String timpPlayer2 = "5:00";
        int xtimpPlayer1 = boardParam.startX + boardParam.boardSize + boardParam.margin;
        int ytimpPlayer1  = boardParam.startY + boardParam.boardSize/2 + cellSize;
        int xtimpPlayer2  = boardParam.startX + boardParam.boardSize + boardParam.margin;
        int ytimpPlayer2 = boardParam.startY + boardParam.boardSize/2;

        g.drawString(timpPlayer1, xtimpPlayer1, ytimpPlayer1);
        g.drawString(timpPlayer2, xtimpPlayer2, ytimpPlayer2);

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

