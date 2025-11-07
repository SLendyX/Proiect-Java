package pieces;

import javax.swing.*;
import java.awt.*;

public abstract class Piece  {
    boolean isWhite;
    Image image;

    Piece(boolean isWhite,String pieceType) {
        this.isWhite = isWhite;
        this.image = new ImageIcon("./data/pieces/"+ (isWhite ? "white" : "black") +"/"+pieceType+".png").getImage();

    }

    public boolean isWhite(){
        return isWhite;
    }

    public Image getImage() {
        return image;
    }

}
