package client.protocol;

import com.google.gson.Gson;

/**
 * Error message for web interface
 */
public class ErrorMessage extends Message {
    private final String type = "error";
    private String message;

    /**
     * Constructor
     * @param message Message that is send to the client
     */
    public ErrorMessage(String message) {
        this.message = message;
    }
}
