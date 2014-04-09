package client.protocol;

/**
 * Interface for network messages
 */
public abstract class NetworkMessage extends Message {
    /**
     * returns the byte representation of the message
     * @return message
     */
    public abstract byte[] toByteArray();
}
