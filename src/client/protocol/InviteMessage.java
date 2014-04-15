package client.protocol;

/**
 * Invites other player to the conversation
 */
public class InviteMessage extends Message {
    public final static String TYPE = "inviteMessage";
    private int conversation;
    private String other;

    /**
     * Constructor
     * @param conversation ID of the conversation
     * @param other Other member of the conversation
     */
    public InviteMessage(int conversation, String other) {
        super(TYPE);
        this.conversation = conversation;
        this.other = other;
    }

    public String getOther() {
        return other;
    }

    public int getConversation() {
        return conversation;
    }
}
