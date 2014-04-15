package client.protocol;

import java.io.Serializable;

/**
 * Marker class for messages
 */
public abstract class Message implements Serializable {
    private final String type;

    public Message(String type) {
        this.type = type;
    }
}
