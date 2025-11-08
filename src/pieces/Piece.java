package pieces;

import javax.swing.*;
import java.awt.*;

public abstract class Piece  {
    boolean isWhite;
    Image image;
    String type;
    char fenChar;


    Piece(boolean isWhite,String pieceType) {
        this.isWhite = isWhite;
        this.image = new ImageIcon("./data/pieces/"+ (isWhite ? "white" : "black") +"/"+pieceType+".png").getImage();
        this.type = pieceType;
        this.fenChar = isWhite ? Character.toUpperCase(pieceType.charAt(0)) : pieceType.charAt(0);
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

}
