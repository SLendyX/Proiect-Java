package pieces;

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

        if(piecesArray[y][x] != null && piecesArray[y][x].isWhite() != isWhite){
            piecesArray[y][x].setIsAttacked(true);
        }

        return piecesArray[y][x] == null;
    }


    public abstract PiecePosition[] getMoves(Piece[][] piecesArray);



    /*
        //for loop care verifica daca exista sau nu inamici in cale
        Stack<PiecePosition> stack = new Stack<>();

        for(...){
            //logica if pentru verificare patrate
            stack.push(new PiecePosition(x,y));
        }
        stack.toArray();
    * */


}
