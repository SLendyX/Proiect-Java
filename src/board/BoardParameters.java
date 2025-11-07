package board;

import java.awt.*;

public class BoardParameters {
    public int
            startX,
            startY,
            boardSize,
            cellSize,
            margin;

    public Color backgroundColor, darkSquare, lightSquare;

    public BoardParameters(int startX, int startY, int boardSize, int cellSize, int margin, Color backgroundColor, Color darkSquare, Color lightSquare) {
        this.startX = startX;
        this.startY = startY;
        this.boardSize = boardSize;
        this.cellSize = cellSize;
        this.margin = margin;
        this.backgroundColor = backgroundColor;
        this.darkSquare = darkSquare;
        this.lightSquare = lightSquare;
    }
}
