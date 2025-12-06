package board;

import engine.ChessEngine;
import engine.GameTimer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SidePanel extends JPanel {
    private final ChessEngine engine;
    private GameTimer gameTimer;
    private final JFrame parentFrame;

    private final JLabel whiteTimerLabel;
    private final JLabel blackTimerLabel;
    private Timer uiUpdateTimer;

    private final JButton btnResign;
    private final JButton btnDraw;
    private final JButton btnBackToMenu;

    public void setGameTimer(GameTimer gameTimer) {
        this.gameTimer = gameTimer;
    }


    public SidePanel(ChessEngine engine, GameTimer gameTimer, JFrame parentFrame, Runnable returnToMenuCallback) {
        this.engine = engine;
        this.gameTimer = gameTimer;
        this.parentFrame = parentFrame;

        // Culoarea fundalului similară cu tabla (sau chiar culoarea background-ului aplicației)
        this.setBackground(new Color(50, 50, 50));
        this.setLayout(new GridBagLayout());
        this.setPreferredSize(new Dimension(200, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- TIMER BLACK ---
        JPanel blackPanel = createTimerPanel("Black", new Color(181, 136, 99)); // Culoare piesa neagra/inchisa
        blackTimerLabel = (JLabel) blackPanel.getComponent(1); // Luam label-ul sa-l updatam
        add(blackPanel, gbc);

        // --- SPACE ---
        gbc.gridy++;
        add(Box.createVerticalStrut(20), gbc);

        // --- TIMER WHITE ---
        JPanel whitePanel = createTimerPanel("White", new Color(240, 217, 181)); // Culoare piesa alba/deschisa
        whiteTimerLabel = (JLabel) whitePanel.getComponent(1);
        add(whitePanel, gbc);

        // --- BUTTONS ---
        gbc.gridy++;
        add(Box.createVerticalStrut(30), gbc); // Spațiu mai mare

        btnDraw = createClassicButton("Propose Draw");
        add(btnDraw, gbc);

        gbc.gridy++;
        btnResign = createClassicButton("Resign");
        add(btnResign, gbc);

        gbc.gridy++;
        btnBackToMenu = createClassicButton("Back to Menu");
        btnBackToMenu.setVisible(false);
        btnBackToMenu.addActionListener(e -> returnToMenuCallback.run());
        add(btnBackToMenu, gbc);

        // Actions
        btnDraw.addActionListener(e -> askForDraw());
        btnResign.addActionListener(e -> resignGame());

        // Pornim un timer de UI care doar reimprospateaza label-urile la fiecare 100ms
        // Nu afecteaza logica jocului, doar afisarea
        startUIUpdateLoop();
    }

    private JPanel createTimerPanel(String playerName, Color bgColor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bgColor);
        p.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        JLabel nameLbl = new JLabel(playerName, SwingConstants.CENTER);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        nameLbl.setForeground(Color.BLACK);
        nameLbl.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));

        JLabel timeLbl = new JLabel("00:00", SwingConstants.CENTER);
        timeLbl.setFont(new Font("Monospaced", Font.BOLD, 24));
        timeLbl.setForeground(Color.BLACK);
        timeLbl.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));

        p.add(nameLbl, BorderLayout.NORTH);
        p.add(timeLbl, BorderLayout.CENTER);
        return p;
    }

    private JButton createClassicButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(Color.LIGHT_GRAY);
        btn.setFocusable(false); // CRUCIAL pentru tasta ESC
        return btn;
    }

    private void startUIUpdateLoop() {
        uiUpdateTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Luam timpul din obiectul GameTimer existent
                long whiteTime = gameTimer.getTimeWhite();
                long blackTime = gameTimer.getTimeBlack();

                whiteTimerLabel.setText(formatTime(whiteTime));
                blackTimerLabel.setText(formatTime(blackTime));

                // Daca jocul s-a terminat, oprim update-ul vizual si afisam butonul de meniu
                if (engine.isGameOver()) {
                    uiUpdateTimer.stop();
                    btnBackToMenu.setVisible(true);
                    btnResign.setEnabled(false);
                    btnDraw.setEnabled(false);
                }
            }
        });
        uiUpdateTimer.start();
    }

    private String formatTime(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void resignGame() {
        if (engine.isGameOver()) return;
        int response = JOptionPane.showConfirmDialog(parentFrame, "Are you sure you want to resign?", "Confirm Resign", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            engine.resign();
            gameTimer.stopTimer(); // Oprim timerul logic
            parentFrame.repaint();
        }
    }

    private void askForDraw() {
        if (engine.isGameOver()) return;
        int response = JOptionPane.showConfirmDialog(parentFrame, "Opponent offers a draw. Accept?", "Draw Offer", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            engine.agreeDraw();
            gameTimer.stopTimer();
            parentFrame.repaint();
        }
    }
}