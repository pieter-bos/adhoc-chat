package client.protocol;

public class LeaveConversationMessage extends Message {
    public static final String TYPE = "leaveConversationMessage";
    private final int convId;

    public LeaveConversationMessage(int convId) {
        super(TYPE);
        this.convId = convId;
    }

    public int getConvId() {
        return convId;
    }
}
