package board;

import engine.ChessEngine;
import engine.GameTimer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import network.NetworkGameState;
import network.NetworkManager;

public class SidePanel extends JPanel {
    private ChessEngine engine;
    private GameTimer gameTimer;
    private final JFrame parentFrame;

    // Referințe pentru a putea modifica fontul dinamic
    private JLabel whiteTimerLabel;
    private JLabel whiteNameLabel;
    private JLabel blackTimerLabel;
    private JLabel blackNameLabel;

    private Timer uiUpdateTimer;

    private final JButton btnResign;
    private final JButton btnDraw;

    private JButton btnAcceptDraw;
    private JButton btnDeclineDraw;
    private NetworkManager networkManager;

    public void setGameTimer(GameTimer gameTimer) {
        this.gameTimer = gameTimer;
    }

    public void setChessEngine(ChessEngine engine){
        this.engine = engine;
    }

    public SidePanel(JFrame parentFrame) {
        this.engine = null;
        this.gameTimer = null;
        this.parentFrame = parentFrame;

        // Culoarea fundalului
        this.setBackground(new Color(50, 50, 50));
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Permite componentelor să se lățească
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Umple pe orizontală
        gbc.insets = new Insets(10, 10, 10, 10); // Margini procentuale ar fi ideale, dar fixe e ok

        // --- TIMER BLACK ---
        // Salvăm referințele direct la creare
        JPanel blackPanel = createTimerPanel("Black", new Color(181, 136, 99), false);
        add(blackPanel, gbc);

        // --- SPACE ---
        gbc.gridy++;
        // Folosim weight-y pentru a împinge elementele, spațiul devine flexibil

        add(Box.createGlue(), gbc);

        // --- TIMER WHITE ---
        JPanel whitePanel = createTimerPanel("White", new Color(240, 217, 181), true);
        add(whitePanel, gbc);

        // --- SPACE ---
        gbc.gridy++;
        add(Box.createGlue(), gbc);

        // --- BUTTONS ---

        btnDraw = createClassicButton("Propose Draw");
        add(btnDraw, gbc);

        gbc.gridy++;
        btnResign = createClassicButton("Resign");
        add(btnResign, gbc);

        // Listeners
        btnDraw.addActionListener(e -> askForDraw());
        btnResign.addActionListener(e -> resignGame());

        // Adăugăm un listener care recalculează fonturile când se redimensionează fereastra
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeFonts();
            }
        });
        btnAcceptDraw = new JButton("Accept Draw");
        btnAcceptDraw.setBackground(new Color(0, 165, 0)); // Portocaliu să iasă în evidență
        btnAcceptDraw.setForeground(Color.WHITE);
        btnAcceptDraw.setFocusable(false);
        btnAcceptDraw.setVisible(false);

        btnDeclineDraw = new JButton("Decline Draw");
        btnDeclineDraw.setBackground(new Color(220, 20, 60)); // Roșu
        btnDeclineDraw.setForeground(Color.WHITE);
        btnDeclineDraw.setFocusable(false);
        btnDeclineDraw.setVisible(false);

        btnAcceptDraw.addActionListener(e -> {
            if (networkManager != null) {
                networkManager.sendGameStatus(NetworkGameState.StatusType.DRAW_ACCEPT);
                hideDrawButtons();
                engine.agreeDraw();
                if(gameTimer != null) gameTimer.stopTimer();
                triggerBoardGameOver();
            }
        });
        btnDeclineDraw.addActionListener(e -> {
            if (networkManager != null) {
                networkManager.sendGameStatus(NetworkGameState.StatusType.DRAW_DECLINE);
                hideDrawButtons(); // Ascunde butoanele
            }
        });
        gbc.gridy++;
        add(btnAcceptDraw, gbc);
        gbc.gridy++;
        add(btnDeclineDraw, gbc);


        startUIUpdateLoop();

    }


    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    // Metodă modificată pentru a salva referințele corecte
    private JPanel createTimerPanel(String playerName, Color bgColor, boolean isWhite) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bgColor);
        p.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        JLabel nameLbl = new JLabel(playerName, SwingConstants.CENTER);
        nameLbl.setForeground(Color.BLACK);
        nameLbl.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));

        JLabel timeLbl = new JLabel("00:00", SwingConstants.CENTER);
        timeLbl.setForeground(Color.BLACK);
        timeLbl.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));

        // Salvăm referințele în câmpurile clasei
        if (isWhite) {
            this.whiteNameLabel = nameLbl;
            this.whiteTimerLabel = timeLbl;
        } else {
            this.blackNameLabel = nameLbl;
            this.blackTimerLabel = timeLbl;
        }

        p.add(nameLbl, BorderLayout.NORTH);
        p.add(timeLbl, BorderLayout.CENTER);
        return p;
    }

    private JButton createClassicButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.LIGHT_GRAY);
        btn.setFocusable(false);
        return btn;
    }

    private void resizeFonts() {
        int width = getWidth();
        int height = getHeight();

        // Evităm erori la inițializare când dimensiunea e 0
        if (width == 0 || height == 0) return;

        // Calculăm o mărime de bază în funcție de lățimea panoului
        // De exemplu: vrem ca fontul să fie cam 1/15 din lățime pentru text normal
        int baseFontSize = Math.max(12, width / 15);
        int timerFontSize = Math.max(16, width / 9);

        Font nameFont = new Font("SansSerif", Font.BOLD, baseFontSize);
        Font timeFont = new Font("Monospaced", Font.BOLD, timerFontSize);
        Font btnFont = new Font("SansSerif", Font.BOLD, baseFontSize);

        // Aplicăm noile fonturi
        if(whiteNameLabel != null) whiteNameLabel.setFont(nameFont);
        if(blackNameLabel != null) blackNameLabel.setFont(nameFont);

        if(whiteTimerLabel != null) whiteTimerLabel.setFont(timeFont);
        if(blackTimerLabel != null) blackTimerLabel.setFont(timeFont);

        if(btnDraw != null) btnDraw.setFont(btnFont);
        if(btnResign != null) btnResign.setFont(btnFont);

        // Forțăm redesenarea componentelor interne
        revalidate();
        repaint();
    }

    void startUIUpdateLoop() {
        System.out.println("gets here");
        uiUpdateTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameTimer != null) {
                    long whiteTime = gameTimer.getTimeWhite();
                    long blackTime = gameTimer.getTimeBlack();
                    whiteTimerLabel.setText(formatTime(whiteTime));
                    blackTimerLabel.setText(formatTime(blackTime));
                }

                if (engine.isGameOver()) {
                    uiUpdateTimer.stop();
                    btnResign.setEnabled(false);
                    btnDraw.setEnabled(false);
                    triggerBoardGameOver();
                }
            }
        });
        uiUpdateTimer.start();
    }

    private void triggerBoardGameOver() {
        for (Component comp : parentFrame.getContentPane().getComponents()) {
            // Căutăm recursiv dacă e într-un wrapper
            if (comp instanceof BoardPanel) {
                ((BoardPanel) comp).showGameOverScreen(engine.getGameState());
                return;
            } else if (comp instanceof Container) {
                // Caz special pentru că acum avem un container wrapper în Menu
                for(Component child : ((Container)comp).getComponents()){
                    if(child instanceof BoardPanel){
                        ((BoardPanel) child).showGameOverScreen(engine.getGameState());
                        return;
                    }
                }
            }
        }
        parentFrame.repaint();
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
            if (networkManager != null) {
                networkManager.sendGameStatus(NetworkGameState.StatusType.RESIGN);
                boolean amIWhite = networkManager.isHost();
                engine.forceResign(amIWhite);
                if (gameTimer != null) gameTimer.stopTimer();
            } else {//singleplayer
                engine.resign();
                if (gameTimer != null) gameTimer.stopTimer();
                triggerBoardGameOver();
            }
        }
    }

    private void askForDraw() {
        if (engine.isGameOver()) return;
        //MULTIPLAYER
        if (networkManager != null) {
            btnDraw.setEnabled(false);
            int confirm = JOptionPane.showConfirmDialog(parentFrame,
                    "Do you want to offer a draw to your opponent?",
                    "Offer Draw", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                networkManager.sendGameStatus(NetworkGameState.StatusType.DRAW_OFFER);
                JOptionPane.showMessageDialog(parentFrame, "Draw offer sent.");
            }
        }
        //SINGLEPLAYER / LOCAL
        else {
            int response = JOptionPane.showConfirmDialog(parentFrame,
                    "Opponent offers a draw. Accept?",
                    "Draw Offer", JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                if(gameTimer != null) gameTimer.stopTimer();
            }
        }
    }
    public void showDrawOffer() {
        btnAcceptDraw.setVisible(true);
        btnDeclineDraw.setVisible(true);
        btnAcceptDraw.revalidate();
        btnAcceptDraw.repaint();
    }

    public void hideDrawButtons() {
        btnAcceptDraw.setVisible(false);
        btnDeclineDraw.setVisible(false);
        revalidate();
        repaint();
    }
    public void enableDrawButton() {
        btnDraw.setEnabled(true);
    }
    public void resetSidePanel() {
        btnResign.setEnabled(true);
        btnDraw.setEnabled(true);

        hideDrawButtons();

        whiteTimerLabel.setText("00:00");
        blackTimerLabel.setText("00:00");


        if (uiUpdateTimer != null && !uiUpdateTimer.isRunning()) {
            System.out.println("Repornire UI Update Loop");
            uiUpdateTimer.restart();
        }

        revalidate();
        repaint();
    }

    public void hideDrawButton(){
        btnDraw.setVisible(false);
    }


}