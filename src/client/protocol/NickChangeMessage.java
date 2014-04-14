package client.protocol;

/**
 * Broadcasted by client to notify others of it's presence on the network
 */
public class NickChangeMessage extends Message {
    private String nick;

    /**
     * Constructor
     * @param nick Nickname of this client
     */
    public NickChangeMessage(String nick) {
        this.nick = nick;
    }

    public String getNick() {
        return nick;
    }
}