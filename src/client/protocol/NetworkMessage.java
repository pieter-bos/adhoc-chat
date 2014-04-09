package client.protocol;

/**
 * Interface for network messages
 */
public interface NetworkMessage extends Message {
    /**
     * returns the byte representation of the message
     * @return message
     */
    public byte[] toByteArray();
}
