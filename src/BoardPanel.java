import javax.swing.*;
import java.awt.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class BoardPanel extends JPanel {
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        printBoard(g);
    }

    public void printBoard(Graphics g){
        int boardSize = min(getWidth(), getHeight());
        int startX = (getWidth() - boardSize) / 2;
        int startY = (getHeight() - boardSize) / 2;


        int margin = boardSize*7/100;
        int cellSize = (boardSize-margin)/8;

        setBackground(new Color(223, 222, 222));
        g.fillRect(startX,startY,boardSize,boardSize);


        for(int row=0;row<8;row++){
            for(int col=0;col<8;col++){
                if((row+col)%2==1){
                    g.setColor(new Color(181, 136, 99));
                }else {
                    g.setColor(new Color(240, 217, 181));
                }

                int xPos = startX + col*cellSize + margin/2;
                int yPos = startY + row*cellSize + margin/2;
                g.fillRect(xPos,yPos,cellSize,cellSize);
            }
        }

        int fontSize = max(10, cellSize/5);

        Font font = new Font("Arial", Font.BOLD, fontSize);
        g.setFont(font);
        g.setColor(new Color(227, 227, 227));

        FontMetrics fm = g.getFontMetrics();

        String columns = "abcdefgh";
        String rows = "87654321";

        for(int cell=0;cell<8;cell++){
            int[] coordinatesColumns = getCoordinatesPos(startX, startY, cell, cellSize, margin, fm, true);
            int[] coordinatesRows = getCoordinatesPos(startX, startY, cell, cellSize, margin, fm, false);

            //columns
            g.drawString(columns.substring(cell, cell+1),coordinatesColumns[0],coordinatesColumns[1]);
            g.drawString(columns.substring(cell, cell+1),coordinatesColumns[0],coordinatesColumns[1] + boardSize - margin/2);
            //rows
            g.drawString(rows.substring(cell, cell+1),coordinatesRows[0],coordinatesRows[1]);
            g.drawString(rows.substring(cell, cell+1),coordinatesRows[0] + boardSize - margin/2,coordinatesRows[1]);
        }
    }

    public static int[] getCoordinatesPos(int x, int y, int index, int size, int margin, FontMetrics fontMetrics, boolean isColumn){
        int[] coordinates = new int[2];

        int width = fontMetrics.stringWidth("a");
        int height = fontMetrics.getAscent();

        if(isColumn){
            coordinates[0]=x+(size*(2*index+1)+margin-width)/2;
            coordinates[1]=y+(margin+height)/4;
        }else{
            coordinates[0]=x+(margin-2*width)/4;
            coordinates[1]=y+(size*(2*index+1)+margin+height)/2;
        }

        return coordinates;
    }

}



//incercare esuata de a crea o bordura
//        g.drawLine(startX+margin/2-1,startY+margin/2 -1,startX+margin/2 -1,startY-margin/2 - 3 + boardSize);
//        g.drawLine(startX+margin/2 -1,startY-margin/2 - 3 + boardSize,startX-margin/2 - 3 + boardSize,startY-margin/2 - 3 + boardSize);
//        g.drawLine(startX-margin/2 - 3 + boardSize,startY-margin/2 - 3 + boardSize,startX-margin/2 - 3 + boardSize,startY+margin/2 - 1);
//        g.drawLine(startX+margin/2 -1 ,startY+margin/2 - 1,startX-margin/2 - 3 + boardSize,startY+margin/2 -1);
