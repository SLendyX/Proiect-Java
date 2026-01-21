package board;

import javax.swing.*;

public class MenuPopUp extends PopUp {

    public MenuPopUp(Runnable firstOption, Runnable secondOption) {
        super(firstOption, secondOption, "Resume", "Exit to Menu");
        setLabelMessage("Game Paused");
    }
}
