package board;

import engine.ChessEngine;

import javax.swing.*;
import java.awt.*;
import pieces.Piece;

public class PromotionPanel extends JPanel {
    ChessEngine chessEngine =  new ChessEngine();
    Piece pawn;

    public PromotionPanel(ChessEngine chessEngine) {
        this.chessEngine = chessEngine;
    }


    public void printPromotionPanel(Graphics g) {
       int posY = pawn.getPostion().y;
       int posX = pawn.getPostion().x;
       int cellSize = chessEngine.getBoardParams().cellSize;
       g.drawRect(posX, posY, cellSize, cellSize);
       for(int i = 0; i < 3 ; i++){
           g.drawImage(pawn.getImage(), posX, posY, cellSize, cellSize, null);
       }

    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        printPromotionPanel(g);
    }

    public void setPawn(Piece pawn){
        this.pawn = pawn;
    }
}
