package client.protocol;

/**
 * Invites other player to the conversation
 */
public class InviteMessage extends Message {
    public final static String TYPE = "inviteMessage";
    private int conversation;
    private String[] members;

    /**
     * Constructor
     * @param conversation ID of the conversation
     * @param members Other members of the conversation
     */
    public InviteMessage(int conversation, String[] members) {
        super(TYPE);
        this.conversation = conversation;
        this.members = members;
    }
}
