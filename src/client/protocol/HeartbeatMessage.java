package client.protocol;

/**
 * Broadcasted by client to notify others of it's presence on the network
 */
public class HeartbeatMessage extends Message {
    private String nick;

    /**
     * Constructor
     * @param nick Nickname of this client
     */
    public HeartbeatMessage(String nick) {
        this.nick = nick;
    }
}
