package board;

import javax.swing.*;
import java.awt.*;

public class GameOverPanel extends PopUp {
    public GameOverPanel(Runnable firstOption, Runnable secondOption) {
        super(firstOption, secondOption, "Try Again", "Exit to Menu");
    }
    public void setTryAgainButtonText(String text) {
        setFirstButtonText(text);
    }

    // Opțional: Metoda pentru a dezactiva butonul
    public void setTryAgainButtonEnabled(boolean enabled) {
        setFirstButtonEnabled(enabled);
    }
}
