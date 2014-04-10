package client.protocol;

/**
 * Tells others that this client is leaving the conversation
 */
public class LeaveMessage extends Message {
    private int conversation;

    /**
     * Constructor
     * @param conversation ID of the conversation
     */
    public LeaveMessage(int conversation) {
        this.conversation = conversation;
    }
}
