package board;

import javax.swing.*;
import java.awt.*;

public class GameOverPanel extends PopUp {
    public GameOverPanel(Runnable firstOption, Runnable secondOption) {
        super(firstOption, secondOption, "Try Again", "Exit to Menu");
    }
}
