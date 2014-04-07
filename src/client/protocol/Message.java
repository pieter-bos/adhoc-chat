package client.protocol;

/**
 * Interface for network messages
 */
public interface Message {

    /**
     * returns the byte representation of the message
     * @return message
     */
    public byte[] toByteArray();

    /**
     * returns the JSON representation of the message
     * @return message
     */
    public String toJSON();
}
