package client.protocol;

/**
 * Tells others that this client is leaving the conversation
 */
public class LeaveMessage extends Message {
    public static final String TYPE = "leaveMessage";
    private String username;

    /**
     * Constructor
     * @param conversation ID of the conversation
     */
    public LeaveMessage(String username) {
        super(TYPE);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
