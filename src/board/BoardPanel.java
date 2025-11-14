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

    public BoardPanel(){
        boardParam = new BoardParameters();
        chessEngine.setBoardParams(boardParam);
        chessEngine.instantiatePieceArray();

        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e){
                handleMouseClick(e);
            }
        });
//        addMouseListener(new MouseAdapter(){
//            @Override
//            public void mouseEntered(MouseEvent e){
//                handleMouseHover(e);
//            }
//        });
    }


//    private void handleMouseHover(MouseEvent e){
//        e.
//    }

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

            if(x < 0 || x > 7 || y < 0 || y > 7){
                throw new OutOfBoardException("Clicked outside the board!");
            }

            Piece piece =  chessEngine.piecesArray[y][x];

            if(chessEngine.doesMoveExist(x, y) && chessEngine.piecesArray[y][x] == null){
                System.out.printf("Moved %s to %s.%n", chessEngine.getMove(x,y).moveAuthor.getType(), chessEngine.getChessCoords(x,y));
                chessEngine.swapSquares(chessEngine.getMove(x, y));
                chessEngine.setMovesArray(null);
                repaint();
            }else if(chessEngine.piecesArray[y][x] == null){
                throw new OutOfPieceMatrixException("Selected square does not contain a chess piece!");
            }else{
                chessEngine.setMovesArray(piece);
                repaint();
            }

//            PiecePosition pos = piece.getPostion();
//            System.out.printf("Clicked on: x:%d  y:%d  chess_coordinates: %s%nPiece is at: x:%d  y:%d  chess_coordinates: %s%n", x, y, chessEngine.getChessCoords(x, y, boardParam.isReversed), pos.x, pos.y, pos.chessCoordinate);

        } catch (OutOfBoardException | OutOfPieceMatrixException ex) {
            chessEngine.setMovesArray(null);
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
//        boardParam.switchBoardOrientation();
        int boardSize = min(getWidth(), getHeight());
        int startX = getWidth() - boardSize - 223 > 200 ? (getWidth() - boardSize)/2 : getWidth() - boardSize - 223;
        int startY = (getHeight() - boardSize) / 2;
        int margin = boardSize*7/100;
        int cellSize = (boardSize-margin)/8;

        this.boardParam = new BoardParameters(startX,
                startY,
                boardSize,
                cellSize,
                margin,
                new Color(223, 222, 222),
                new Color(181, 136, 99),
                new Color(240, 217, 181));

        chessEngine.setBoardParams(boardParam);

        printBoard(g, boardParam);

        printCoordinates(g,
                boardParam,
                "SansSerif",
                new Color(227, 227, 227));

        printPieces(g, boardParam, chessEngine.piecesArray);
        printTimer(g, boardParam);

        printMoves(chessEngine.getMovesArray(), g, boardParam);
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
        /**
         * marimea fontului este un procent din marimea unui patrat de joc
        **/
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
            /**
             * Calculam pozitia coordonatelor
             * */
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
            int x = move.piecePosition.x;
            int y = move.piecePosition.y;


            int xPos = getXPos(boardParam, x);
            int yPos = getYPos(boardParam, y);

            int size = boardParam.cellSize*13/36;

            xPos = xPos + (boardParam.cellSize - size) /2;
            yPos = yPos + (boardParam.cellSize - size) /2;

//            g.fillRect(xPos,yPos,boardParam.cellSize,boardParam.cellSize);
            g.fillArc(xPos,yPos,size,size, 0, 360);
        });


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

