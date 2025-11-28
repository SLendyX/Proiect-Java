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

    // Mecanism pentru a notifica BoardPanel cand primeste o mutare
    private Consumer<NetworkMove> moveReceivedCallback;

    public NetworkManager(boolean isHost) {
        this.isHost = isHost;
    }

    public void setMoveReceivedCallback(Consumer<NetworkMove> callback) {
        this.moveReceivedCallback = callback;
    }

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
        new Thread(this::listenForMoves).start();
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

    // Ruleaza pe un thread separat si asculta mutari
    private void listenForMoves() {
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
            }
        } catch (ClassNotFoundException | IOException e) {
            if (socket != null && !socket.isClosed()) {
                System.err.println("Conexiune pierduta sau eroare: " + e.getMessage());
            }
        }
    }

    public boolean isHost() {
        return isHost;
    }
}
