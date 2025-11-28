package board;

import network.NetworkManager;
import java.io.IOException;
import javax.swing.SwingUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Menu extends JPanel {
    private final JFrame parentFrame;

    public Menu(JFrame frame){
        this.parentFrame = frame;
        setLayout(new GridBagLayout());

        setBackground(new Color(50,50,50));
        JLabel title = new JLabel("Menu");
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 14));
        title.setForeground(Color.WHITE);

        JButton startGame = new JButton("Start Game");
        JButton hostGame = new JButton("Host Game!");
        JButton joinGame = new JButton("Join Game!");
        JButton robotGame = new  JButton("Play vs robot!");
        JButton exitGame = new JButton("Exit");

        Dimension buttonSize = new Dimension(250, 50);
        Font buttonFont = new Font("SansSerif", Font.PLAIN, 20);

        startGame.setPreferredSize(buttonSize);
        hostGame.setPreferredSize(buttonSize);
        joinGame.setPreferredSize(buttonSize);
        robotGame.setPreferredSize(buttonSize);
        exitGame.setPreferredSize(buttonSize);

        startGame.setFont(buttonFont);
        hostGame.setFont(buttonFont);
        joinGame.setFont(buttonFont);
        robotGame.setFont(buttonFont);
        exitGame.setFont(buttonFont);

        GridBagConstraints grid = new GridBagConstraints();
        grid.insets = new Insets(15,15,15,15);
        grid.gridx = 0;
        grid.gridy = 0;
        add(title, grid);

        grid.gridy=1;
        startGame.addActionListener(e -> startGame());
        add(startGame, grid);

        grid.gridy=2;
        hostGame.addActionListener(e -> hostGame(true, "localhost"));
        add(hostGame, grid);

        grid.gridy=3;
        joinGame.addActionListener(e -> {
            String ipAddress = JOptionPane.showInputDialog(parentFrame, "Introduceti adresa IP a Host-ului (e.g., 192.168.1.100):", "Conectare la Joc", JOptionPane.QUESTION_MESSAGE);
            if (ipAddress != null && !ipAddress.trim().isEmpty()) {
                hostGame(false, ipAddress.trim());
            }
        });
        add(joinGame, grid);

        grid.gridy=4;
        robotGame.addActionListener(e -> robotGame());
        add(robotGame, grid);

        grid.gridy=5;
        exitGame.addActionListener(e -> System.exit(0));
        add(exitGame, grid);
    }

    public void startGame(){
        parentFrame.getContentPane().removeAll();
        BoardPanel board = new BoardPanel(parentFrame);
        parentFrame.add(board);
        parentFrame.revalidate();
        parentFrame.repaint();
        board.requestFocusInWindow();
    }

    private void robotGame() {
    }

    // Functie pentru a porni jocul in retea (RULEAZA PE UN THREAD SEPARAT!)
    private void hostGame(boolean isHost, String ipAddress) {
        // Afiseaza un dialog de asteptare, deoarece conexiunea blocheaza
        JDialog loadingDialog = new JDialog(parentFrame, "Conectare...", true);
        loadingDialog.setLayout(new FlowLayout());
        loadingDialog.add(new JLabel(isHost ? "Astept jucatorul advers (client)..." : "Ma conectez la Host..."));
        loadingDialog.setSize(300, 100);
        loadingDialog.setLocationRelativeTo(parentFrame);

        new Thread(() -> {
            try {
                // Initializare NetworkManager
                NetworkManager networkManager = new NetworkManager(isHost);
                networkManager.start(ipAddress); // Aici se blocheaza pana la conectare

                // Odata conectat, schimba interfata in Thread-ul principal (EDT)
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose(); // Inchide dialogul
                    showBoardPanel(networkManager);
                });

            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    loadingDialog.dispose(); // Inchide dialogul
                    JOptionPane.showMessageDialog(parentFrame, "Eroare la conectare: " + ex.getMessage() + "\nAsigurati-va ca host-ul ruleaza si IP-ul este corect.", "Eroare Retea", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();

        loadingDialog.setVisible(true); // Afiseaza dialogul
    }

    private void showBoardPanel(NetworkManager networkManager) {
        // 1. Inlatura meniul
        parentFrame.getContentPane().removeAll();

        // 2. Instantiere si adaugare BoardPanel
        BoardPanel board = new BoardPanel(parentFrame, networkManager); // Transmite managerul de retea
        parentFrame.add(board);

        // 3. Revalideaza layout-ul
        parentFrame.revalidate();
        parentFrame.repaint();

        board.requestFocusInWindow();
    }
}
