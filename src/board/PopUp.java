package board;

import network.NetworkManager;

import javax.swing.*;
import java.awt.*;

public abstract class PopUp extends JPanel{
    private final JLabel messageLabel;
    protected final JButton firstBtn;
    protected final JButton secondBtn;
    private NetworkManager networkManager;

    public PopUp(Runnable firstOption, Runnable secondOption, String firstOptionMessage, String secondOptionMessage) {
        setLayout(new GridBagLayout());
        setOpaque(false);
        setVisible(false);

        JPanel contentBox = new JPanel();
        contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
        contentBox.setBackground(new Color(255, 255, 255, 230));
        contentBox.setBorder(BorderFactory.createEmptyBorder(20,40,20,40));

        messageLabel = new JLabel("Game Over");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 24));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        firstBtn = new JButton(firstOptionMessage);
        firstBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        firstBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        firstBtn.addActionListener(e -> {
            // Daca e multiplayer, putem lasa vizibil pana este acceptat
            setVisible(false);
            firstOption.run();
        });

        secondBtn = new JButton(secondOptionMessage);
        secondBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        secondBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        secondBtn.addActionListener(e -> secondOption.run());

        contentBox.add(messageLabel);
        contentBox.add(Box.createVerticalStrut(20));
        contentBox.add(firstBtn);
        contentBox.add(Box.createVerticalStrut(10));
        contentBox.add(secondBtn);

        add(contentBox);
    }

    public void setLabelMessage(String msg){
        messageLabel.setText(msg);
    }

    public void setFirstButtonText(String text) {
        firstBtn.setText(text);
        firstBtn.repaint();
    }

    // Metodă pentru a dezactiva butonul
    public void setFirstButtonEnabled(boolean enabled) {firstBtn.setEnabled(enabled);}
    public void setFirstButtonVisible(boolean enabled) {firstBtn.setVisible(enabled);}

    @Override
    protected void paintComponent(Graphics g){
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}
