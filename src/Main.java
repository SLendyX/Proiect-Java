import javax.swing.*;
import java.awt.*;

public class Main {
    private final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chess");
        BoardPanel board = new BoardPanel();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setMinimumSize(new Dimension(800,800));
//        frame.setSize(800,800);

        frame.setResizable(true);
        frame.setVisible(true);

        frame.setLocation(
                (screenSize.width-frame.getWidth())/2,
                (screenSize.height-frame.getHeight())/2
        );

        frame.add(board);

//        board.repaint();


    }
}
