package network;

import java.io.Serial;
import java.io.Serializable;

public class NetworkGameState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public enum StatusType {
        RESIGN,
        DRAW_OFFER,
        DRAW_ACCEPT,
        DRAW_DECLINE,
        REMATCH_REQUEST,
        REMATCH_ACCEPT
    }

    public StatusType statusType;

    public NetworkGameState(StatusType statusType) {
        this.statusType = statusType;
    }
}