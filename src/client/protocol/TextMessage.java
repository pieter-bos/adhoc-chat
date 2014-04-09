package client.protocol;

/**
 * Sends a text message to the other conversation members
 */
public class TextMessage extends NetworkMessage {
    private final String type = "text";
    private int conversation;
    private String message;

    /**
     * Constructor
     * @param conversation ID of the conversation
     * @param message Contents of the message
     */
    public TextMessage(int conversation, String message) {
        this.conversation = conversation;
        this.message = message;
    }

    @Override
    public byte[] toByteArray() {
        return new byte[0];
    }
}
