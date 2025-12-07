package AI;

import java.io.*;

public class StockFish {
    private Process process;
    private volatile BufferedReader reader;
    private BufferedWriter writer;

    /**
     * Starts the Stockfish engine.
     * @param path The absolute or relative path to the Stockfish executable.
     * @return true if started successfully, false otherwise.
     */

    public boolean startEngine(String path, int difficulty) {
        try {
            ProcessBuilder builder = new ProcessBuilder(path);

            System.out.println(builder.directory());


            this.process = builder.start();
            this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            // Handshake with UCI
            sendCommand("uci");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends a command to Stockfish.
     */

    public void sendCommand(String command) {
        try {
            if(writer != null) {
                writer.write(command + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Asks Stockfish for the best move for a given FEN position.
     * This is blocking; consider running in a separate thread.
     * * @param fen The board position in FEN format.
     * @param depth Search depth (e.g., 10-20).
     * @return The best move string (e.g., "e2e4").
     */

    public void setElo(int difficulty){
        sendCommand("setoption name UCI_LimitStrength value true");
        sendCommand("setoption name UCI_Elo value " + difficulty);

        System.out.println("Set the AI to " + difficulty + " elo.");
    }

    public String getBestMove(String fen, int depth) {
        sendCommand("position fen " + fen);
        sendCommand("go depth " + depth);

        String line;
        try {
            // Read lines until we find the "bestmove"
            while (reader == null) Thread.onSpinWait();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    // Output format: "bestmove e2e4 ponder e7e5"
                    return line.split(" ")[1];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Stops the engine and cleans up resources.
     */
    public void stopEngine() {
        try {
            sendCommand("quit");
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (process != null) process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
