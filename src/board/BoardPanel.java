package board;
import pieces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import engine.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class BoardPanel extends JPanel {
    String defaultFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

    BoardParameters boardParam = null;
    Piece[][] piecesArray = null;
    ChessEngine chessEngine = new ChessEngine();


    public BoardPanel(){
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

            if(x < 0 || x > 7 || y < 0 || y > 7){
                throw new OutOfBoardException("Clicked outside the board!");
            }

            if(piecesArray[y][x] == null){
                throw new OutOfPieceMatrixException("Selected square does not contain a chess piece!");
            }

            PiecePosition pos = piecesArray[y][x].getPostion();

            System.out.printf("Clicked on: x:%d  y:%d  chess_coordinates: %s%nPiece is at: x:%d  y:%d  chess_coordinates: %s%n", x, y, chessEngine.getChessCoords(x, y, boardParam.isReversed), pos.x, pos.y, pos.chessCoordinate);
        } catch (OutOfBoardException | OutOfPieceMatrixException ex) {
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
        int startX = getWidth() - boardSize - 223 > 200 ? (getWidth() - boardSize)/2 : getWidth() - boardSize - 223;
        int startY = (getHeight() - boardSize) / 2;
        int margin = boardSize*7/100;
        int cellSize = (boardSize-margin)/8;

        boardParam = new BoardParameters(startX,
                startY,
                boardSize,
                cellSize,
                margin,
                new Color(223, 222, 222),
                new Color(181, 136, 99),
                new Color(240, 217, 181));

        boardParam.switchBoardOrientation();

        printBoard(g, boardParam);

        printCoordinates(g,
                boardParam,
                "SansSerif",
                new Color(227, 227, 227));

        piecesArray = instantiatePieceArray(defaultFen);

        printPieces(g, boardParam, piecesArray);
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
                g.fillRect(xPos,yPos,boardParam.cellSize,boardParam.cellSize);
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


    public Piece[][] instantiatePieceArray(String fen){
        Piece [][] pieces = new Piece[8][8];

        int row=0, col=0;
        for(String fenRow : fen.split("/")){
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

        return pieces;
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

    public void printPieces(Graphics g, BoardParameters boardParam, Piece[][] pieces){
        int pieceCount =0;

        if(boardParam.isReversed){
            for(int row = 7;row >=0;row--){
                for(int col = 7;col >=0;col--){
                    pieceCount = getPieceCount(g, boardParam, pieces, pieceCount, row, col);

                }
            }
        }else{
            for(int row = 0;row < 8;row++){
                for(int col = 0;col < 8;col++){
                    pieceCount = getPieceCount(g, boardParam, pieces, pieceCount, row, col);

                }
            }
        }
    }

    private int getPieceCount(Graphics g, BoardParameters boardParam, Piece[][] pieces, int pieceCount, int row, int col) {
        if(pieces[row][col] != null){
            pieceCount++;
            boolean isReversed = boardParam.isReversed;
            int xPos = getXPos(boardParam, col);
            int yPos = isReversed ? getYPos(boardParam, 7-row) :getYPos(boardParam, row);

            g.drawImage(pieces[row][col].getImage(), xPos, yPos, boardParam.cellSize, boardParam.cellSize, this);
        }
        return pieceCount;
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

//TODO: generare ceas
//5:00
//5:00

        /*
        int fontSize = max(10, cellSize/5);

        Font font = new Font("Arial", Font.BOLD, fontSize);
        g.setFont(font);
        g.setColor(new Color(227, 227, 227));

        String timp = "..";

        xPos = startX + boardSize + 20
        yPos = startY + boardSize/2
                startY + boardSize/2 + cellSize

        g.drawString("string", xPos, yPos)

        * */