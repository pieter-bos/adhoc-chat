package client.protocol;

/**
 * Interface for messages
 */
public interface Message {
    /**
     * returns the JSON representation of the message
     * @return message
     */
    public String toJSON();
}
