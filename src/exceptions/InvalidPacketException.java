package exceptions;

public class InvalidPacketException extends ADCException {
    public InvalidPacketException() {
        super("Invalid packet.");
    }

    public InvalidPacketException(String msg) {
        super(msg);
    }
}
