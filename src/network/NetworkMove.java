package network;

import java.io.Serial;
import java.io.Serializable;

public class NetworkMove implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public int fromX;
    public int fromY;
    public int toX;
    public int toY;

    public boolean isCapture;
    public boolean isEnpassant;
    public boolean isCastle;

    public NetworkMove(int fromX, int fromY, int toX, int toY, boolean isCapture, boolean isEnpassant, boolean isCastle) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.isCapture = isCapture;
        this.isEnpassant = isEnpassant;
        this.isCastle = isCastle;
    }
}
