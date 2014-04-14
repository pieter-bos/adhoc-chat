package client;

import client.protocol.TextMessage;

import java.util.LinkedList;
import java.util.List;

/**
 * Keeps track of the state of a single conversation
 */
public class Conversation {
    private String user;
    private List<client.protocol.Message> messages;
    private int id;

    /**
     * Constructor
     * @param user Person who you're talking to
     */
    public Conversation(String user, int id) {
        this.user = user;
        this.id = id;
        this.messages = new LinkedList<>();
    }

    public String getUser() {
        return user;
    }

    public int getId() {
        return id;
    }

    public void addMessage(TextMessage message) {
        messages.add(message);
    }
}
