package client.protocol;

/**
 * Tells others that this client is leaving the conversation
 */
public class LeaveMessage extends NetworkMessage {
    private int conversation;

    /**
     * Constructor
     * @param conversation ID of the conversation
     */
    public LeaveMessage(int conversation) {
        this.conversation = conversation;
    }
    @Override
    public byte[] toByteArray() {
        return new byte[0];
    }
}
