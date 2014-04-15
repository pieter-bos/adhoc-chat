package client.protocol;

/**
 * Tells others that this client is leaving the conversation
 */
public class LeaveMessage extends Message {
    private final String type = "leaveMessage";
    private String username;

    /**
     * Constructor
     * @param conversation ID of the conversation
     */
    public LeaveMessage(String username) {
        super("leaveMessage");
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
