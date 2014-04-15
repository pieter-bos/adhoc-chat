package client.protocol;

import java.io.Serializable;

/**
 * Marker class for messages
 */
public class Message implements Serializable {
    private final String type;

    private Message() {
        type = "impossibleType";
    }

    public Message(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
