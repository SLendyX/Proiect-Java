package board;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

public class AIMenu extends JPanel {
    private final JFrame parentFrame;
    private final Menu menuPanel;
    private final JSlider difficultySlider;
    private JLabel eloLabel;

    // Button Group for Color Selection
    private final ButtonGroup colorGroup;
    private final JToggleButton whiteButton;
    private final JToggleButton blackButton;
    private final JToggleButton randomButton;

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

        // --- 1. Title ---
        JLabel titleLabel = new JLabel("Select Difficulty (Stockfish ELO)");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 0;
        add(titleLabel, gbc);

        // --- 2. Difficulty Slider ---
        difficultySlider = new JSlider(JSlider.HORIZONTAL, MIN_ELO, MAX_ELO, INITIAL_ELO);
        difficultySlider.setMajorTickSpacing(500);
        difficultySlider.setMinorTickSpacing(250);
        difficultySlider.setSnapToTicks(true);
        difficultySlider.setPaintTicks(true);
        difficultySlider.setPaintLabels(true);

        difficultySlider.setPreferredSize(new Dimension(450, 70));
        difficultySlider.setBackground(new Color(50, 50, 50));
        difficultySlider.setForeground(Color.WHITE);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(MIN_ELO, new JLabel(MIN_ELO + " ELO"));
        labelTable.put(MAX_ELO, new JLabel(MAX_ELO + " ELO"));
        difficultySlider.setLabelTable(labelTable);

        difficultySlider.addChangeListener(e -> {
            if (!difficultySlider.getValueIsAdjusting()) {
                eloLabel.setText("Selected ELO: " + difficultySlider.getValue());
            }
        });

        gbc.gridy = 1;
        gbc.insets = new Insets(15, 15, 5, 15);
        add(difficultySlider, gbc);

        // --- 3. ELO Label ---
        eloLabel = new JLabel("Selected ELO: " + INITIAL_ELO);
        eloLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        eloLabel.setForeground(Color.WHITE);
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 15, 15, 15);
        add(eloLabel, gbc);

        // --- 4. Color Selection Buttons (NEW) ---
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        colorPanel.setBackground(new Color(50, 50, 50));

        colorGroup = new ButtonGroup();

        // White Button
        whiteButton = createColorButton("White", "src/data/pieces/white/pawn.png");
        // Black Button
        blackButton = createColorButton("Black", "src/data/pieces/black/pawn.png");
        // Random Button (No image, just text/symbol)
        randomButton = createColorButton("Random", "src/data/background/random.png");
        styleButton(randomButton);
        randomButton.setSelected(true); // Default to Random

        colorGroup.add(whiteButton);
        colorGroup.add(randomButton);
        colorGroup.add(blackButton);

        colorPanel.add(whiteButton);
        colorPanel.add(randomButton);
        colorPanel.add(blackButton);

        gbc.gridy = 3; // Shifted down
        add(colorPanel, gbc);

        // --- 5. Start AI Game Button ---
        JButton startGameButton = getJButton();

        gbc.gridy = 4; // Shifted down
        gbc.insets = new Insets(15, 15, 15, 15);
        add(startGameButton, gbc);

        // --- 6. Back to Menu Button ---
        JButton backButton = new JButton("Back to Menu");
        backButton.setPreferredSize(new Dimension(250, 50));
        backButton.setFont(new Font("SansSerif", Font.PLAIN, 20));

        backButton.addActionListener(e -> {
            menuPanel.showMenu();
        });

        gbc.gridy = 5; // Shifted down
        add(backButton, gbc);
    }

    private JButton getJButton() {
        JButton startGameButton = new JButton("Start AI Game");
        startGameButton.setPreferredSize(new Dimension(250, 50));
        startGameButton.setFont(new Font("SansSerif", Font.PLAIN, 20));

        startGameButton.addActionListener(e -> {
            int selectedElo = difficultySlider.getValue();
            boolean selectedColor = getSelectedColor();

            System.out.println("Starting AI game. ELO: " + selectedElo + ", Color: " + selectedColor);
            startAIGame(selectedElo, selectedColor);
        });
        return startGameButton;
    }

    // Helper to create styled toggle buttons with images
    private JToggleButton createColorButton(String toolTip, String imagePath) {
        JToggleButton btn = new JToggleButton();
        btn.setToolTipText(toolTip);
        styleButton(btn);

        // Load and Scale Image
        ImageIcon icon = new ImageIcon(imagePath);
        if (icon.getIconWidth() > 0) {
            Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        } else {
            // Fallback if image path is wrong
            btn.setText(toolTip);
        }

        return btn;
    }

    // Helper to style the color buttons consistently
    private void styleButton(JToggleButton btn) {
        btn.setPreferredSize(new Dimension(80, 60));
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    // Helper to get selected color string
    private boolean getSelectedColor() {
        if (whiteButton.isSelected()) return true;
        if (blackButton.isSelected()) return false;
        return Math.random() < 0.5;
    }

    private void startAIGame(int difficultyElo, boolean playerColor) {
        // Logic to start game
        parentFrame.getContentPane().removeAll();

        // Note: You might need to update your BoardPanel constructor to accept the playerColor!
        // For now, this is kept generic based on your snippet.

        BoardPanel board = new BoardPanel(parentFrame, playerColor, difficultyElo);
        parentFrame.add(board);
        parentFrame.revalidate();
        parentFrame.repaint();
        board.requestFocusInWindow();
    }
}