package ai;

import java.io.*;
import java.nio.file.*;

public class StockFish {
    private Process process;
    private volatile BufferedReader reader;
    private BufferedWriter writer;

    public String getEnginePath() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String resourcePath = getResourcePath(os);

        InputStream in = getClass().getResourceAsStream(resourcePath);

        if (in == null) {
            throw new IOException("Engine not found inside JAR at: " + resourcePath +
                    "\nCHECK: Did you move the 'data' folder INSIDE the 'src' folder?");
        }

        // 3. Create a temporary file on the real hard drive
        File tempFile = File.createTempFile("stockfish_engine", os.contains("win") ? ".exe" : "");
        tempFile.deleteOnExit(); // Clean up on exit

        // 4. Copy the binary data from the JAR to the temp file
        Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        in.close();

        // 5. Make it executable (Critical for Mac/Linux)
        tempFile.setExecutable(true);

        // 6. Return the path to the TEMP file, which ProcessBuilder can run
        return tempFile.getAbsolutePath();
    }

    private static String getResourcePath(String os) {
        String resourcePath = "";

        // 1. Determine the path INSIDE the JAR (Note: No "src", starts with "/")
        if (os.contains("win")) {
            resourcePath = "/data/ai/stockfish/windows/stockfish-windows-x86-64-avx2.exe";
        } else if (os.contains("mac")) {
            resourcePath = "/data/ai/stockfish/macos/stockfish-macos-m1-apple-silicon";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            resourcePath = "/data/ai/stockfish/linux/stockfish-ubuntu-x86-64-avx2";
        } else {
            throw new RuntimeException("Sistem de operare necunoscut: " + os);
        }
        return resourcePath;
    }

    public boolean startEngine(int difficulty) {
        try {
            ProcessBuilder builder = new ProcessBuilder(getEnginePath());

            System.out.println(builder.directory());

            setElo(difficulty);

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
