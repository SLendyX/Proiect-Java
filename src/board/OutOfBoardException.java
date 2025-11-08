package board;

public class OutOfBoardException extends Exception {
    public OutOfBoardException(String message) {
        super(message);
    }
}
