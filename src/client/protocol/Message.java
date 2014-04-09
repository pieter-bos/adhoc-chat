package client.protocol;

import com.google.gson.Gson;

/**
 * Interface for messages
 */
public abstract class Message {
    /**
     * returns the JSON representation of the message
     * @return message
     */
    public String toJSON() {
        return new Gson().toJson(this);
    }
}
