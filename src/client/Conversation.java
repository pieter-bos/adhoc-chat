package client;

import java.util.LinkedList;
import java.util.List;

/**
 * Keeps track of the state of a single conversation
 */
public class Conversation {
    private String user;
    private List<Message> messages;

    /**
     * Constructor
     * @param user Person who you're talking to
     */
    public Conversation(String user) {
        this.user = user;
        this.messages = new LinkedList<>();
    }
}
