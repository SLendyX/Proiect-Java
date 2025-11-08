package board;
import pieces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class BoardPanel extends JPanel {
    String defaultFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
    BoardParameters boardParam = null;
    Piece[][] piecesArray = null;

    public BoardPanel(){
        addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e){
                handleMouseClick(e);
            }
        });
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


    private void handleMouseClick(MouseEvent e){
        int cellSize = boardParam.cellSize;
        int margin = boardParam.margin;
        int startX = boardParam.startX;
        int startY = boardParam.startY;

        try{
            if(e.getX() < startX + margin/2 || e.getX() > startX + boardParam.boardSize - margin/2 ||
                    e.getY() < startY + margin/2 || e.getY() > startY + boardParam.boardSize - margin/2) {
                throw new OutOfBoardException("Clicked outside the board");
            }
            int x = (e.getX() - startX - margin/2)/cellSize;
            int y = (e.getY() - margin/2)/cellSize;

            if(x < 0 || x > 7 || y < 0 || y > 7){
                throw new OutOfBoardException("Clicked outside the board");
            }

            System.out.printf("x:%d  y:%d  chess_coordintes: %s%n", x, y, getChessCoords(x, y));
        } catch (OutOfBoardException ex) {
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
                if((row+col)%2==1){
                    g.setColor(boardParam.darkSquare);
                }else {
                    g.setColor(boardParam.lightSquare);
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

        String columns = "abcdefgh";
        String rows = "87654321";

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
                    pieces[row][col++] = instantiatePiece(fenCol);
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
        for(int row=0;row<8;row++){
            for(int col=0;col<8;col++){
                if(pieces[row][col] != null){
                    pieceCount++;
                    int xPos = getXPos(boardParam, col);
                    int yPos = getYPos(boardParam, row);

                    g.drawImage(pieces[row][col].getImage(), xPos, yPos, boardParam.cellSize, boardParam.cellSize, this);
                }

            }
        }
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