package client.protocol;

/**
 * Broadcasted by client to notify others of it's presence on the network
 */
public class NickChangeMessage extends Message {
    private final String type = "nickChangeMessage";
    private String username;

    /**
     * Constructor
     * @param nick Nickname of this client
     */
    public NickChangeMessage(String nick) {
        this.username = nick;
    }

    public String getUsername() {
        return username;
    }
}
