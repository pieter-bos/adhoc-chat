package client.protocol;

/**
 * Sends a text message to the other conversation members
 */
public class TextMessage extends Message {
    private final String type = "textMessage";
    private int convId;
    private String nickname;
    private String message;

    /**
     * Constructor
     * @param message Contents of the message
     */
    public TextMessage(String message, String nickname, int convId) {
        super("textMessage");
        this.message = message;
        this.nickname = nickname;
        this.convId = convId;
    }

    public int getConvId() {
        return convId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }
}
