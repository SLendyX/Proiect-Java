package board;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

public class AIMenu extends JPanel {
    private final JFrame parentFrame;
    private final Menu menuPanel;
    private final JSlider difficultySlider;
    private JLabel eloLabel;

    private static final int MIN_ELO = 250;
    private static final int MAX_ELO = 3000;
    private static final int INITIAL_ELO = 1250;

    public AIMenu(JFrame frame, Menu menu) {
        this.parentFrame = frame;
        this.menuPanel = menu;

        setLayout(new GridBagLayout());
        setBackground(new Color(50, 50, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;

        // --- 1. Titlu ---
        JLabel titleLabel = new JLabel("Select Difficulty (Stockfish ELO)");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 0;
        add(titleLabel, gbc);

        // --- 2. Slider de Dificultate (Vertical) ---

        difficultySlider = new JSlider(JSlider.HORIZONTAL, MIN_ELO, MAX_ELO, INITIAL_ELO);
        difficultySlider.setMajorTickSpacing(500);
        difficultySlider.setMinorTickSpacing(250);

        difficultySlider.setSnapToTicks(true);
        difficultySlider.setPaintTicks(true);
        difficultySlider.setPaintLabels(true);

        // SCHIMBARE DIMENSIUNE: Lățimea redusă și înălțimea crescută
        difficultySlider.setPreferredSize(new Dimension(450, 70));
        difficultySlider.setBackground(new Color(50, 50, 50));
        difficultySlider.setForeground(Color.WHITE);

        // Etichete personalizate pentru a afișa doar capetele intervalului
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(MIN_ELO, new JLabel(MIN_ELO + " ELO"));
        labelTable.put(MAX_ELO, new JLabel(MAX_ELO + " ELO"));
        difficultySlider.setLabelTable(labelTable);

        // Listener: Actualizează eticheta ELO când utilizatorul eliberează mouse-ul
        difficultySlider.addChangeListener(e -> {
            if (!difficultySlider.getValueIsAdjusting()) {
                eloLabel.setText("Selected ELO: " + difficultySlider.getValue());
            }
        });

        gbc.gridy = 1;
        // Permite butoanelor de jos să fie mai aproape de slider, centrarea rămâne.
        gbc.insets = new Insets(15, 15, 5, 15);
        add(difficultySlider, gbc);

        // --- 3. Eticheta ELO (Valoare Curenta) ---
        eloLabel = new JLabel("Selected ELO: " + INITIAL_ELO);
        eloLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        eloLabel.setForeground(Color.WHITE);
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 15, 15, 15);
        add(eloLabel, gbc);

        // --- 4. Buton Start AI Game ---
        JButton startGameButton = new JButton("Start AI Game");
        startGameButton.setPreferredSize(new Dimension(250, 50));
        startGameButton.setFont(new Font("SansSerif", Font.PLAIN, 20));

        startGameButton.addActionListener(e -> {
            int selectedElo = difficultySlider.getValue();
            System.out.println("Starting AI game with ELO: " + selectedElo);
            startAIGame(selectedElo);
        });

        gbc.gridy = 3;
        gbc.insets = new Insets(15, 15, 15, 15);
        add(startGameButton, gbc);

        // --- 5. Buton Back to Menu ---
        JButton backButton = new JButton("Back to Menu");
        backButton.setPreferredSize(new Dimension(250, 50));
        backButton.setFont(new Font("SansSerif", Font.PLAIN, 20));

        backButton.addActionListener(e -> {
            menuPanel.showMenu();
        });

        gbc.gridy = 4;
        add(backButton, gbc);
    }

    private void startAIGame(int difficultyElo) {
        // Logica pentru a porni jocul (rămâne de implementat pentru AI)
        parentFrame.getContentPane().removeAll();
        BoardPanel board = new BoardPanel(parentFrame); // Placeholder
        parentFrame.add(board);
        parentFrame.revalidate();
        parentFrame.repaint();
        board.requestFocusInWindow();
    }
}