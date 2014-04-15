package client.protocol;

/**
 * Broadcasted by client to notify others of it's presence on the network
 */
public class NickChangeMessage extends Message {
    public static final String TYPE = "nickChangeMessage";
    private String username;

    /**
     * Constructor
     * @param nick Nickname of this client
     */
    public NickChangeMessage(String nick) {
        super(TYPE);
        this.username = nick;
    }

    public String getUsername() {
        return username;
    }
}
