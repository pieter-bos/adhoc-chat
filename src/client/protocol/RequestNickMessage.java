package client.protocol;

public class RequestNickMessage extends Message {
    public static final String TYPE = "requestNickMessage";

    public RequestNickMessage() {
        super(TYPE);
    }
}
