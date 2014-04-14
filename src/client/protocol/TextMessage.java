package client.protocol;

/**
 * Sends a text message to the other conversation members
 */
public class TextMessage extends Message {
    private String message;

    /**
     * Constructor
     * @param message Contents of the message
     */
    public TextMessage(String message) {
        this.message = message;
    }
}
