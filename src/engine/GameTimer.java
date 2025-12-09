package engine;
import board.BoardPanel;

public class GameTimer implements Runnable{
    private final BoardPanel boardPanel;
    private final ChessEngine chessEngine;
    private long timeWhite; // Timpul în milisecunde pentru jucatorul Alb
    private long timeBlack; // Timpul în milisecunde pentru jucatorul Negru
    private boolean isRunning;
    private final long INITIAL_TIME = 5 * 60 * 1000; // 5 minute in milisecunde
    private boolean paused;
    
    public GameTimer(ChessEngine chessEngine, BoardPanel boardPanel) {
        this.chessEngine = chessEngine;
        this.boardPanel = boardPanel;
        this.timeWhite = INITIAL_TIME;
        this.timeBlack = INITIAL_TIME;
        this.isRunning = true;
    }

    public void startTimer() {
        if (!isRunning) return;
        new Thread(this).start();
    }

    public void resetTimer(){
        if(isRunning) this.isRunning = false;
        timeWhite = INITIAL_TIME;
        timeBlack = INITIAL_TIME;
    }

    public void stopTimer(){
        isRunning = false;
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        while (isRunning) {
            while(paused){
                lastTime = System.currentTimeMillis();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            long currentTime = System.currentTimeMillis();
            long delta = currentTime - lastTime;
            lastTime = currentTime;

            if (delta < 0) delta = 0; // Previne problemele la schimbari de ora

            if (chessEngine.getTurn()) { // true for White's turn
                timeWhite -= delta;
            } else { // false for Black's turn
                timeBlack -= delta;
            }

            // Verifica daca a expirat timpul
            if (timeWhite <= 0 || timeBlack <= 0) {
                timeWhite = timeWhite < 0 ? 0 : timeWhite;
                timeBlack = timeBlack < 0 ? 0 : timeBlack;
                isRunning = false; // Opreste jocul
                chessEngine.playEndSound();
                boardPanel.showGameOverScreen(getWinner());
            }

            // Declanseaza re-desenarea panoului pentru a actualiza ceasul
            boardPanel.repaint();

            try {
                // Seteaza o mica pauza pentru a nu folosi resurse excesive (e.g., 50ms)
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                isRunning = false;
            }
        }
    }

    public long getTimeWhite() {
        return timeWhite;
    }

    public long getTimeBlack() {
        return timeBlack;
    }

    public int getWinner(){
        if(getTimeWhite() == 0){
            return 5;
        }else if(getTimeBlack() == 0){
            return 4;
        }
        return 0;
    }

    public void togglePause(){
        this.paused = !this.paused;
    }

}
