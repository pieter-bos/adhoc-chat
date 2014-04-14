package client;

/**
 * Message
 */
public class Message {
    private String sender;
    private String contents;

    /**
     * Constructor
     * @param sender Original sender of the message
     * @param contents Contents of the message
     */
    public Message(String sender, String contents) {
        this.sender = sender;
        this.contents = contents;
    }
}
