package board;

import javax.swing.*;
import java.awt.*;

public class GameOverPanel extends JPanel {
        private final Runnable onRestart;
        private final Runnable onExit;
        private final JLabel messageLabel;

        public GameOverPanel(Runnable onRestart, Runnable onExit) {
        this.onRestart = onRestart;
        this.onExit = onExit;

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

        JButton restartBtn = new JButton("Try Again");
        restartBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        restartBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        restartBtn.addActionListener(e -> {
            setVisible(false);
            onRestart.run();
        });

        JButton exitBtn = new JButton("Exit to Menu");
        exitBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        exitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitBtn.addActionListener(e -> onExit.run());

        contentBox.add(messageLabel);
        contentBox.add(Box.createVerticalStrut(20));
        contentBox.add(restartBtn);
        contentBox.add(Box.createVerticalStrut(10));
        contentBox.add(exitBtn);

        add(contentBox);
    }

    public void setMessage(String msg){
        messageLabel.setText(msg);
    }

    @Override
    protected void paintComponent(Graphics g){
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}
