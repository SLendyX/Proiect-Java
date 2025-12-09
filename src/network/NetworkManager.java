package network;

import engine.Move;
import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkManager {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private final boolean isHost;
    private Runnable connectionLostCallback;

    // Mecanism pentru a notifica BoardPanel cand primeste o mutare
    private Consumer<NetworkMove> moveReceivedCallback;

    private Consumer<NetworkGameState> statusReceivedCallback;
    public NetworkManager(boolean isHost) {
        this.isHost = isHost;
    }
    public void setConnectionLostCallback(Runnable callback) {this.connectionLostCallback = callback;}
    public void setMoveReceivedCallback(Consumer<NetworkMove> callback) {
        this.moveReceivedCallback = callback;
    }
    public void setStatusReceivedCallback(Consumer<NetworkGameState> callback) {this.statusReceivedCallback = callback;}

    public void start(String ipAddress) throws IOException {
        if (isHost) {
            ServerSocket serverSocket = new ServerSocket(8888); // Port standard: 8888
            System.out.println("Asteptare conexiune client pe portul 8888...");
            socket = serverSocket.accept(); // Asteapta pana se conecteaza cineva
            serverSocket.close();
            System.out.println("Client conectat!");
        } else {
            System.out.println("Conectare la host: " + ipAddress + ":8888...");
            socket = new Socket(ipAddress, 8888);
            System.out.println("Conectat la host!");
        }

        // Ordinea este importanta: OutputStream inainte de InputStream
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());

        // Porneste un thread separat pentru a asculta mutari primite
        new Thread(this::listenForData).start();
    }

    // Trimite mutarea adversarului
    public void sendMove(Move move) {
        try {
            // Extrage coordonatele necesare din obiectul Move
            int fromX = move.moveAuthor().getPostion().x;
            int fromY = move.moveAuthor().getPostion().y;
            int toX = move.piecePosition().x;
            int toY = move.piecePosition().y;

            NetworkMove netMove = new NetworkMove(
                    fromX, fromY, toX, toY,
                    move.isCapture(), move.isEnpassant(), move.isCastle()
            );

            outputStream.writeObject(netMove);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Eroare la trimiterea mutarii.");
        }
    }

    public void sendGameStatus(NetworkGameState.StatusType type) {
        try {
            NetworkGameState status = new NetworkGameState(type);
            outputStream.writeObject(status);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Eroare la trimiterea statusului.");
        }
    }

    // Ruleaza pe un thread separat si asculta mutari
    public void listenForData() {
        try {
            while (socket.isConnected()) {
                // Asteapta primirea unui obiect (mutare)
                Object receivedObject = inputStream.readObject();
                if (receivedObject instanceof NetworkMove netMove) {
                    // Executa actiunea pe firul de executie Swing (EDT)
                    SwingUtilities.invokeLater(() -> {
                        if (moveReceivedCallback != null) {
                            moveReceivedCallback.accept(netMove);
                        }
                    });
                }
                else if (receivedObject instanceof NetworkGameState netStatus) {
                    SwingUtilities.invokeLater(() -> {
                        if (statusReceivedCallback != null) {
                            statusReceivedCallback.accept(netStatus);
                        }
                    });
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            System.err.println("Conexiune întreruptă: " + e.getMessage());
            if (connectionLostCallback != null) {
                SwingUtilities.invokeLater(connectionLostCallback);
            }
        }
    }

    public void disconnect() {
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException ignored) {}

        try {
            if (outputStream != null) outputStream.close();
        } catch (IOException ignored) {}

        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}

        System.out.println("NetworkManager: all connections closed.");
    }

    public boolean isHost() {
        return isHost;
    }
}
