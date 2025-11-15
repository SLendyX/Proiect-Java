package pieces;

import engine.ChessEngine;
import engine.PiecePosition;

import javax.swing.*;
import java.awt.*;

public abstract class Piece  {
    boolean isWhite;
    Image image;
    String type;
    char fenChar;
    PiecePosition piecePosition;
    boolean isAttacked = false;
    boolean hasMoved = false;

    Piece(boolean isWhite,String pieceType) {
        this.isWhite = isWhite;
        this.image = new ImageIcon("./data/pieces/"+ (isWhite ? "white" : "black") +"/"+pieceType+".png").getImage();
        this.type = pieceType;

        if(pieceType.equals("knight")){
            this.fenChar = isWhite ? Character.toUpperCase(pieceType.charAt(1)) : pieceType.charAt(1);
        }else{
            this.fenChar = isWhite ? Character.toUpperCase(pieceType.charAt(0)) : pieceType.charAt(0);
        }
    }

    public boolean hasMoved(){
        return this.hasMoved;
    }

    public void setHasMoved(boolean hasMoved){
        this.hasMoved = hasMoved;
    }


    public boolean isWhite(){
        return isWhite;
    }

    public Image getImage() {
        return image;
    }

    public String getType() {
        return type;
    }

    public char getFenChar() {
        return fenChar;
    }

    public void setPosition(int x, int y){
        this.piecePosition = new PiecePosition(x, y) ;
    }

    public void setPosition(int x, int y, boolean isReversed){
        this.piecePosition = new PiecePosition(x,y, isReversed);
    }

    public PiecePosition getPostion(){
        return this.piecePosition;
    }

    public boolean getIsAttacked(){
        return this.isAttacked;
    }

    public void setIsAttacked(boolean isAttacked){
        this.isAttacked = isAttacked;
    }

    public boolean canMove(int x, int y, Piece[][] piecesArray){
        if(x < 0 || x>7 || y < 0 || y>7){
            return false;
        }

        return piecesArray[y][x] == null;
    }

    public void setImage(String pieceType){
        this.image = new ImageIcon("./data/pieces/"+ (isWhite ? "white" : "black") +"/"+pieceType+".png").getImage();
    }


    public abstract PiecePosition[] getMoves(Piece[][] piecesArray);
}
